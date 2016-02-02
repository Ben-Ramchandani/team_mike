public interface JobScheduler {
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
	
    public static Job getJob(Device d) {
    	// Apply algorithm to match device with best job, or just random for now (Ben)
    	
    	
    	// returns null if there is not job match;
		return null;
		
	}
    
    //Mimic the exceptions thrown by Copmutation.submitJob()
    //If the job fails submission then th JobScheduler will note it.
    //If a job fails repeatedly the computation will be failed.
    public void submitJob();
}
