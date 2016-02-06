package twork;

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
    and timing out jobs that dont come back for too long.
    */
    
	/* 
	 * Make this singleton. 
	 * Pass in the device to getJob() so we can allow at some point the users to choose computations
	 * Maybe it shouldn't be an interface as we will have a way to find priority for the Job type -> so only one way to Schedule.
	*/
	
	//The device  contains the job ID, check they're correct then hand over to here.
	public void submitJob(Device d, byte[] data) {
		
	}
	
    public Job getJob(Device d) {
    	// Apply algorithm to match device with best job, or just random for now (Ben)
    	
    	
    	// returns null if there is not job match;
		return null;
		
	}
}
