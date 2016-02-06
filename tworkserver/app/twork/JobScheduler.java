package twork;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.avaje.ebean.Ebean;

import models.Data;
import models.Job;

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

	/* 
	 * Make this singleton. 
	 * Pass in the device to getJob() so we can allow at some point the users to choose computations
	 * Maybe it shouldn't be an interface as we will have a way to find priority for the Job type -> so only one way to Schedule.
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
			}
		}

		public void save() {
			//Conversion from byte[] to string is nasty.
			Job j = Job.find.byId(jobID);
			if(j == null) {
				System.err.println("Job save failed: unable to locate job.");
				failed = true;
				throw new RuntimeException();
			}
			Data d;
			UUID dataID = UUID.randomUUID();
			try {
				d = Data.store(result, dataID, j.computationID);
			} catch (IOException e) {
				System.err.println("Job save failed: unable to store data.");
				e.printStackTrace();
				failed = true;
				throw new RuntimeException();
			}
			d.save();
			j.outputDataID = dataID;
			j.update();
		}

	}

	Map<UUID, ScheduleJob> jobMap;
	//Jobs that want more phones.
	List<ScheduleJob> waitingJobs;
	//Jobs that are just waiting for results.
	List<ScheduleJob> activeJobs;
	
	//How many completed jobs were in the database on last rebuild?
	private int deadJobCount;
	
	private JobScheduler() {
		rebuild();
	}
	
	
	//Completely rebuild state from the Database
	//Looses track of active jobs -> they will be refused.
	//Only intended for server restart.
	//TODO: Implement non-destructive version.
	//TODO: only public for testing
	public synchronized void rebuild() {
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
	public synchronized void timeoutJob(UUID jobID) {
		ScheduleJob j = jobMap.get(jobID);
		if(j != null) {
			j.timeout();
			activeJobs.remove(j);
			processJob(j);
		}
	}

	private synchronized void processJob(ScheduleJob j) {
		if(j.isComplete()) {
			j.save();
			//TODO: Notify computation manager
		} else if(j.isFailed()) {
			//TODO: Notify computation manager
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
			j.addPhone();
			activeJobs.add(j);
			d.registerJob(j.getJobID());
			return Job.find.byId(j.getJobID());
		}
	}
}
