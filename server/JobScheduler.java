public interface JobScheduler {
    /*
    Manages running jobs.
    The network handler will call getJob() to fetch a job, and submitJob() when a completed job comes back.
    This module will manage requesting Jobs from the Computation module, keeping track of who jobs have sent to
    and timing out jobs that dont come back for too long.
    */
    
    public Job getJob() throws JobNotAvailableException;
    
    //Mimic the exceptions thrown by Copmutation.submitJob()
    //If the job fails submission then th JobScheduler will note it.
    //If a job fails repeatedly the computation will be failed.
    public void submitJob();
}