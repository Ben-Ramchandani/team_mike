package team_mike.server;

import java.util.*;

public class SimpleScheduler implements JobScheduler {

	private static final long timeoutMilisec = 5 * 60 * 1000;
	private static final long maxFailCount = 3;
	private ComputationManager computationManager;
	private DatabaseModule database;
	
	//Decorator for jobs
	private class ScheduleJob {
		private Job job;
	

		//What phone has this job?
		private boolean hasPhone;
		private long timestamp;

		private boolean failed;
		private boolean complete;
		
		private int failCount;
		
		public ScheduleJob(Job j) {
			job = j;
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
		
		public Job getJob() {
			return job;
		}

		public void addPhone() {
			timestamp = (new Date()).getTime();
			hasPhone = true;
		}


		private void checkTimestamp() {
			if(timestamp + timeoutMilisec > (new Date()).getTime()) {
				hasPhone = false;
				failCount += 1;
			}
		}

		private void checkFailed() {
			if(failCount >= maxFailCount) {
				failed = true;
			}
		}

		private void update() {
			checkTimestamp();
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
				try {
					job.addReturnData(r);
				} catch (FormatInvalidException e) {
					failCount += 1;
					checkFailed();
					return;
				}
				complete = true;
			}
		}
	}


	Map<Job, ScheduleJob> jobMap;
	//Jobs that want more phones.
	List<ScheduleJob> waitingJobs;
	//Jobs that are just waiting for results.
	List<ScheduleJob> activeJobs;
	
	public SimpleScheduler(ComputationManager c, DatabaseModule d) {
		computationManager = c;
		database = d;
		jobMap = new HashMap<Job, ScheduleJob>();
		waitingJobs = new LinkedList<ScheduleJob>();
		activeJobs = new LinkedList<ScheduleJob>();
	}

	private synchronized void fetchJob() {
		Job j = computationManager.getJob();
		if(j == null) {//No jobs available
			return;
		}
		ScheduleJob sj = new ScheduleJob(j);
		if(!sj.needsPhone()) {
			database.log("SimpleScheduler: New job reports complete");
			jobFailed(sj);
		} else {
			waitingJobs.add(sj);
		}
	}
	
	private synchronized void checkTimestamps() {
		Iterator<ScheduleJob> it = activeJobs.iterator();
		while(it.hasNext()) {
			ScheduleJob j = it.next();
			if(j.needsPhone()) {
				it.remove();
				waitingJobs.add(j);
				database.log("SimpleScheduler: Job timed out");
			} else if(j.isFailed()) {
				it.remove();
				database.log("SimpleScheduler: Job timed out");
				jobFailed(j);
			}
		}
	}
	
	@Override
	public synchronized JobData getJob(long phoneID) {
		if(waitingJobs.isEmpty()) {
			fetchJob();
			checkTimestamps();
		}
		
		if(waitingJobs.size() > 0) {
			ScheduleJob j = waitingJobs.get(0);
			j.addPhone();
			activeJobs.add(j);
			waitingJobs.remove(j);
			return j.getJob();
		} else {
			return null;
		}
	}
	
	private synchronized void jobComplete(ScheduleJob j, long phoneID) {
		computationManager.submitJob(j.getJob(), phoneID);
	}
	
	private synchronized void jobFailed(ScheduleJob j) {
		database.log("SimpleScheduler: No good result for job.");
		computationManager.jobFailed(j.getJob());
	}
	
	@Override
	public synchronized void submitJob(JobData jd, long phoneID, String result) {
		Job job = (Job) jd;
		ScheduleJob j = jobMap.get(job);
		
		if(activeJobs.contains(j)) {
			j.addResult(result);
			activeJobs.remove(j);
			
			if(j.needsPhone()) {
				waitingJobs.add(j);
			} else {
				
				if(j.isComplete()) {
					jobComplete(j, phoneID);
				} else {//j has failed
					jobFailed(j);
				}
			}
		} else {
			database.log("SimpleScheduler: Non-existent job submitted.");
		}
	}
}
