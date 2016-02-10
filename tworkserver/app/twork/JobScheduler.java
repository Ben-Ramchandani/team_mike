package twork;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import models.Data;
import models.Job;

import com.avaje.ebean.Ebean;

public class JobScheduler {


	private static JobScheduler instance = null;
	public static final UUID NULL_UUID = new UUID(0L, 0L); 

	public static JobScheduler getInstance() {
		if (instance == null) {
			instance = new JobScheduler();
		}
		return instance;
	}

	/*
    Manages running jobs.
    The network handler will call getJob() to fetch a job, and submitJob() when a completed job comes back.
    This module will manage requesting Jobs from the Computation module, keeping track of who jobs have sent to
    and timing out jobs that don't come back for too long.
	 */

	private static final long maxFailCount = 3;

	//Decorator for jobs
	//Stored in memory
	//TODO: Only public for testing
	public class ScheduleJob {
		private UUID jobID;


		//What phone has this job?
		private boolean hasPhone;

		private boolean failed;
		private boolean complete;

		private int failCount;
		private String result;

		public ScheduleJob(UUID jID) {
			jobID = jID;
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

		public void addResult(String r) {
			if(!complete && !failed) {
				//Validate here
				result = r;
				complete = true;
				save();
			}
		}

		public void save() {
			Job j = Job.find.byId(jobID);
			if(j == null) {
				System.err.println("Job save failed: unable to locate job.");
				failed = true;
				throw new RuntimeException();
			}

			Data d;
			UUID dataID = UUID.randomUUID();
			try {
				//TODO: uses data class
				d = Data.store(result, dataID, j.computationID);
			} catch (IOException e) {
				System.err.println("Job save failed: unable to store data.");
				e.printStackTrace();
				failed = true;
				throw new RuntimeException();
			}
			j.outputDataID = dataID;

			Ebean.beginTransaction();
			try {
				d.save();
				j.update();
				Ebean.commitTransaction();
			} finally {
				Ebean.endTransaction();
			}
		}

	}

	public Map<UUID, ScheduleJob> jobMap;
	//Jobs that want more phones.
	private List<ScheduleJob> waitingJobs;
	//Jobs that are just waiting for results.
	private List<ScheduleJob> activeJobs;

	//How many completed jobs were in the database on last rebuild?
	private int deadJobCount;

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
				ScheduleJob sj = new ScheduleJob(currentJob.jobID);
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
			newScheduleJobs.add(new ScheduleJob(j.jobID));
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


	//Get the number of jobs in the scheduler
	public int getNumberOfJobs() {
		return activeJobs.size() + waitingJobs.size();
	}

	//Get the number of jobs that have been given out.
	public int getNumberOfActiveJobs() {
		return activeJobs.size();
	}

	//Get the number of jobs that are in the database, but will not be scheduled.
	public int getNumberOfCompletedJobs() {
		return deadJobCount;
	}

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


	//The device  contains the job ID, check they're correct then hand over to here.
	public synchronized void submitJob(Device d, String result) {
		ScheduleJob j = jobMap.get(d.currentJob);
		if(j == null) {
			System.err.println("Submitted job is not in scheduler, ignoring.");
			return;
		}
		if(!activeJobs.contains(j)) {
			System.err.println("Submitted job is not in scheduler, ignoring.");
			return;
		}

		j.addResult(result);
		activeJobs.remove(j);
		processJob(j);
	}


	//Returns null if no jobs are available
	public synchronized Job getJob(Device d) {
		if(waitingJobs.isEmpty()) {
			return null;
		} else {
			ScheduleJob j = waitingJobs.remove(0);
			Job job = Job.find.byId(j.getJobID());
			if(job == null) {
				MyLogger.log("Job in scheduler is not in database");
				return null;
			}
			j.addPhone();
			//Dependent on job only being out once at a time.
			activeJobs.add(j);
			d.registerJob(j.getJobID());
			return job;
		}
	}
}
