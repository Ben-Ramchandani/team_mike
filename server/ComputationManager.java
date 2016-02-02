package team_mike.server;

public interface ComputationManager {
    /*
    This module manages active computations, fetching jobs from them and commiting results from the database,
    as well as handling failures.
    The scheduler interacts with this module, the network handlers shouldn't touch it directly
    */
    
    //The scheduler requests jobs here
    //Get a job from any of the available computations.
    //Return null if no jobs are available
    public Job getJob();
    
    
    //Pass the job on to the relevant computation object.
	//Write the job/phoneID to the Job part of the database.
    public void submitJob(Job j, long phoneID);
    
    //If a job fails repeatedly.
    //Take the computation out the pool, log it and mark as failed in DB.
    public void jobFailed(Job j);
    
    
    //This method called by the ComputationTemplateManager/whatever we're calling it.
    //Adds a computation to the internal list and marks it as active in the DB .
    public void addComputation(Computation c);
}