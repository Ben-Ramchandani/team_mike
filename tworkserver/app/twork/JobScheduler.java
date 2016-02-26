package twork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import models.Computation;
import models.Data;
import models.Device;
import models.Job;
import sitehelper.ImageFactory;

import com.avaje.ebean.Ebean;



/*
Manages running jobs.
The network handler will call getJob() to fetch a job, and submitJob() when a completed job comes back.
This module will manage requesting Jobs from the Computation module, keeping track of who jobs have sent to
and timing out jobs that don't come back for too long.
 */
public class JobScheduler {


	//Singleton
	private static JobScheduler instance = null;

	public static JobScheduler getInstance() {
		if (instance == null) {
			instance = new JobScheduler();
		}
		return instance;
	}


	//Number of times a Job may fail (not come back/fail verification)
	//before it (and its computation) are discarded.
	private static final long maxFailCount = 5;

	

	/*
	 * Member variables
	 */
	public Map<UUID, ScheduleJob> jobMap;
	//Jobs that want more phones.
	private List<ScheduleJob> waitingJobs;
	//Jobs that are just waiting for results.
	private List<ScheduleJob> activeJobs;

	//How many completed/failed jobs were in the database on last rebuild?
	private int deadJobCount;




	/*
	 * Info functions
	 */

	//Get the number of jobs in the scheduler.
	public int getNumberOfJobs() {
		return activeJobs.size() + waitingJobs.size();
	}

	//Get the number of jobs that are currently out on devices.
	public int getNumberOfActiveJobs() {
		return activeJobs.size();
	}

	//Get the number of jobs that are in the database, but will not be scheduled.
	public int getNumberOfCompletedJobs() {
		return deadJobCount;
	}




	/*
	 * Job getting and submitting
	 */

	//The device  contains the job ID, check they're correct then hand over to here.
	public synchronized void submitJob(Device d, byte[] result) {

		ScheduleJob j = jobMap.get(d.currentJob);
		if(j == null) {
			System.out.println("Submitted job is not in scheduler, ignoring.");
			return;
		}
		if(!activeJobs.contains(j)) {
			System.out.println("Submitted job is not in scheduler, ignoring.");
			return;
		}

		j.addResult(result);


		//Notify the webclient.
		try {
			Job job = Ebean.find(Job.class, j.jobID);
			ImageFactory.notify(job.computationID.toString(), job.outputDataID);
		} catch(Exception e) {
			System.out.println("JobScheduler: Error calling notify code. Will continue.");
			e.printStackTrace();
		}


		activeJobs.remove(j);
		processJob(j);
	}



	//Returns null if no jobs are available
	public synchronized Job getJob(Device d) {
		if(waitingJobs.isEmpty()) {
			return null;
		} else {
			ScheduleJob j = waitingJobs.remove(0);
			
			//Check it's in the DB
			Job job = Ebean.find(Job.class, j.getJobID());
			if(job == null) {
				MyLogger.log("Job in scheduler is not in database");
				return null;
			}
			j.addPhone();
			//Dependent on jobs only being out once at a time.
			activeJobs.add(j);
			d.registerJob(j.getJobID());
			return job;
		}
	}
	
	//Get a job from certain computations
	@Deprecated
	public synchronized Job getJob(Device d, List<Computation> c) {
		Iterator<ScheduleJob> it = waitingJobs.iterator();
		while(it.hasNext()) {
			ScheduleJob currentJob = it.next();
			if(c.contains(currentJob.computationID)) {
				
				//Check it's in the DB
				Job job = Ebean.find(Job.class, currentJob.getJobID());
				if(job == null) {
					MyLogger.log("Job in scheduler is not in database");
					return null;
				}
				
				currentJob.addPhone();
				//Dependent on jobs only being out once at a time.
				it.remove();
				activeJobs.add(currentJob);
				d.registerJob(currentJob.getJobID());
				return job;
			}
		}
		return null;
	}







	/*
	 * Job timeout
	 */

	//Called from the Device timeout.
	//This can be called on a job that has already been processed
	//due to nasty concurrency things happening.
	public synchronized void timeoutJob(UUID jobID) {
		ScheduleJob j = jobMap.get(jobID);

		if(j != null) {
			//Dependent on job only being out once at a time.
			if(activeJobs.contains(j)) {
				j.timeout();
				activeJobs.remove(j);
				processJob(j);
			}
		}
	}





	/*
	 * Internal stuff
	 */

	//Decorator for jobs
	//Stored in memory
	public class ScheduleJob {
		private UUID jobID;
		private UUID computationID;

		//What phone has this job?
		private boolean hasPhone;

		private boolean failed;
		private boolean complete;

		private int failCount;
		private byte[] result;

		public ScheduleJob(UUID jID, UUID cID) {
			jobID = jID;
			computationID = cID;
			failed = false;
			complete = false;
			hasPhone = false;
			failCount = 0;
		}

		public boolean isComplete() {
			return complete;
		}

		public boolean isFailed() {
			return failed;
		}

		public UUID getJobID() {
			return jobID;
		}

		public void addPhone() {
			hasPhone = true;
		}

		public void timeout() {
			if(!complete) {
				hasPhone = false;
				failCount++;
				checkFailed();
			}
		}

		private void checkFailed() {
			if(failed || failCount >= maxFailCount) {
				failed = true;
			}
		}


		public boolean needsPhone() {
			if(!failed && !complete) {
				return !hasPhone;
			} else {
				return false;
			}
		}

		public void addResult(byte[] r) {
			if(!complete && !failed) {
				//Validate here
				result = r;
				complete = true;
				save();
			}
		}

		public void save() {
			Job j = Ebean.find(Job.class, jobID);
			if(j == null) {
				System.out.println("Job save failed: unable to locate job.");
				failed = true;
				throw new RuntimeException();
			}		


			Ebean.beginTransaction();
			try {
				j.outputDataID = Data.store(result);
				j.update();
				Ebean.commitTransaction();
			} finally {
				Ebean.endTransaction();
			}
		}

	}



	private JobScheduler() {
		rebuild_TEST();
	}


	//Completely rebuild state from the Database
	//Looses track of active jobs -> they will be refused.
	//Only intended for server restart.
	//Only public for testing
	public synchronized void rebuild_TEST() {
		List<Job> jobs = Ebean.find(Job.class).findList();
		Iterator<Job> it = jobs.iterator();
		jobMap = new HashMap<UUID, ScheduleJob>();
		waitingJobs = new LinkedList<ScheduleJob>();
		activeJobs = new LinkedList<ScheduleJob>();
		deadJobCount = 0;


		while(it.hasNext()) {
			Job currentJob = it.next();
			if(!currentJob.failed && currentJob.outputDataID.equals(Device.NULL_UUID)) {
				ScheduleJob sj = new ScheduleJob(currentJob.jobID, currentJob.computationID);
				UUID currentJobID = currentJob.jobID;
				jobMap.put(currentJobID, sj);
				waitingJobs.add(sj);
			} else {
				deadJobCount++;
			}
		}
	}

	//Update jobs from the database.
	//Needs to be called when a computation fails and its jobs have been removed.
	//Or when a computation is added.
	//Designed to be cheap to add jobs, it will not hold the lock for very long.
	//Slower if lots of jobs are removed (shouldn't happen much)
	public void update() {
		//Make a copy of our job UUIDs
		List<UUID> currentJobIDs;
		synchronized(this) {
			Set<UUID> ks = jobMap.keySet();
			currentJobIDs = new LinkedList<UUID>(ks);
		}

		//Read in the jobs from the database
		//Could be slow, so release lock.
		List<Job> jobs = Ebean.find(Job.class).findList();
		Iterator<Job> it = jobs.iterator();
		List<Job> newJobs = new LinkedList<Job>();
		deadJobCount = 0;

		while(it.hasNext()) {
			Job job = it.next();
			if(!job.failed && job.outputDataID.equals(Device.NULL_UUID)) {

				if(!currentJobIDs.contains(job.jobID)) {
					//New job we haven't seen before.
					newJobs.add(job);
				} else {
					//Already knew about this one
					//Remove it from current jobs,
					//At the end currentJobs will contain all removed jobs.
					currentJobIDs.remove(job.jobID);
				}
			} else {
				deadJobCount++;
			}



		}


		//Adjust internal lists
		List<UUID> removedJobIDs = currentJobIDs;
		List<ScheduleJob> newScheduleJobs = new LinkedList<ScheduleJob>();
		for(Job j : newJobs) {
			newScheduleJobs.add(new ScheduleJob(j.jobID, j.computationID));
		}
		synchronized(this) {
			//Remove jobs as necessary.
			for(UUID u : removedJobIDs) {
				ScheduleJob sj = jobMap.get(u);
				if(sj != null) {
					jobMap.remove(u);
					activeJobs.remove(sj);
					waitingJobs.remove(sj);
				}
			}
			//Add new jobs
			for(ScheduleJob sj : newScheduleJobs) {
				jobMap.put(sj.jobID, sj);
				waitingJobs.add(sj);
			}
		}
	}


	private synchronized void processJob(ScheduleJob j) {
		if(j.isComplete()) {
			ComputationManager.getInstance().jobCompleted(j.getJobID());
		} else if(j.isFailed()) {
			ComputationManager.getInstance().jobFailed(j.getJobID());
		} else if(j.needsPhone()) {
			waitingJobs.add(j);
		} else {
		    activeJobs.add(j);
		}
	}

}
