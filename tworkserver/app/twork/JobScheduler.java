package twork;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import models.Data;
import models.Job;

public class JobScheduler {


	private JobScheduler() {
	}

	private static JobScheduler instance = null;

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
	private class ScheduleJob {
		private long jobID;


		//What phone has this job?
		private boolean hasPhone;

		private boolean failed;
		private boolean complete;

		private int failCount;
		private String result;

		public ScheduleJob(long jID) {
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

		public long getJobID() {
			return jobID;
		}

		public void addPhone() {
			hasPhone = true;
		}
		
		public void timeout() {
			if(!complete) {
				hasPhone = false;
				failCount++;
			}
		}

		private void checkFailed() {
			if(failCount >= maxFailCount) {
				failed = true;
			}
		}

		private void update() {
			checkFailed();
		}

		public boolean needsPhone() {
			if(!failed && !complete) {
				update();
				return hasPhone;
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
			try {
				d = Data.store(result, jobID, j.computationID);
			} catch (IOException e) {
				System.err.println("Job save failed: unable to store data.");
				e.printStackTrace();
				failed = true;
				throw new RuntimeException();
			}
			d.save();
			j.outputData = d;
			j.save();
		}

	}
	
	Map<Long, ScheduleJob> jobMap;
	//Jobs that want more phones.
	List<ScheduleJob> waitingJobs;
	//Jobs that are just waiting for results.
	List<ScheduleJob> activeJobs;
	
	public synchronized void timeoutJob(long jobID) {
		ScheduleJob j = jobMap.get(jobID);
		j.timeout();
		activeJobs.remove(j);
		waitingJobs.add(j);		
	}

	//The device  contains the job ID, check they're correct then hand over to here.
	public synchronized void submitJob(Device d, byte[] data) {

	}

	public synchronized Job getJob(Device d) {
		if(waitingJobs.isEmpty()) {
			return null;
		} else {
			ScheduleJob j = waitingJobs.remove(0);
			j.addPhone();
			activeJobs.add(j);
			return j.getJob();
		} else {
			return null;
		}
		return null;

	}
}
