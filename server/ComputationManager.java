public interface ComputationManager {
    /*
    This module manages active computations, fetching jobs from them and commiting results from the database,
    as well as handling failures.
    The scheduler interacts with this module, the network handlers shouldn't touch it directly
    */
    
    //The scheduler requests jobs here
    //Get a job from any of the available computations.
    //Only throw the exception if none of them have jobs available.
    public Job getJob() throws JobNotAvailableException;
    
    //Just pass through exceptions from Computation.submitJob()
    //Pass the job on to the relevant computation object.
    public void submitJob(Job j) throws JobInvalidException, FormatInvalidException;
    
    //If a job fails repeatedly.
    //Take the computation out the pool, log it and mark as failed in DB.
    public void jobFailed(Job j);
    
    
    //This method called by the ComputationTemplateManager/whatever we're calling it.
    //Adds a computation to the internal list and marks it as active in the DB .
    public void addComputation(Computation c);
}