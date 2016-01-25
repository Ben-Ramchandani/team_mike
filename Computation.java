public interface Computation {
    //The interface for the computation Object given to us by the client.
    //All methods:
    //MUST be thread safe.
    //MUST not block.
    
    //A constant, unique identifier associated with the object.
    public long getComputationID();
    
    //Request a job from this computation.
    //If the Computation needs other jobs to complete first, or is exausted, then JobNotAvailableException should be throw.
    public Job getJob() throws JobNotAvailableException;

    //Have all jobs been given out (not necessarily been handed back in).
    public boolean isExausted();
    
    //Check whether this computation is complete.
    public boolean isComplete();
    
    //Get the result of the computation.
    //Computation MUST be complete.
    public String getResult();
    
    //Get the unique name for this computation.
    public String getName();
    
    //Get the data associated with this computation.
    //This should only be data common to all jobs this object gives.
    //Should be constant.
    public String getComputationData();
    
    
    //Submit a completed job, validating if possible.
    //If a job fails validation the server should retry on another phone.
    //If a job fails repeatedly the whole computation should be suspended and this MUST be logged by the server.
    //If a job has the correct format but the wrong result then return JobInvalidException.
    public void submitJob(Job toSubmit) throws FormatInvalidException, JobInvalidException;
    
    //Is there code for validating the result of a job?
    public boolean isJobValidationEnabled();
    
    
    //Reset all active jobs.
    //This can be used if a server restart looses the list of active jobs.
    //If this is not possible the computation should reset.
    public void reset();
}
