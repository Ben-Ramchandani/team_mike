public interface Computation {
    //The interface for the computation Object, either a template that we make, or given to us directly by the customer.
    //All methods:
    //MUST be thread safe.
    //MUST not block.
    
    //A constant, unique identifier associated with the object.
    //If there are multiple computations of the same type then they may share a name, but not an ID.
    //For example if we have a generic MapReduce template, there will be many computations with the name "MapReduce",
    //but each will have a unique ID.
    public long getComputationID();
    
    //Get the name for this computation.
    public String getName();
    
    //Request a job from this computation.
    //If the Computation needs other jobs to complete first, or is exhausted, then JobNotAvailableException should be thrown.
    public Job getJob() throws JobNotAvailableException;

    //Have all jobs been given out (not necessarily been handed back in).
    public boolean isExausted();
    
    //Check whether this computation is complete.
    public boolean isComplete();
    
    //Get the result of the computation.
    //Computation MUST be complete.
    public String getResult();
    

    
    //Get the data associated with this computation.
    //This should only be data common to all jobs this object gives.
    //Should be constant.
    public String getComputationData();
    
    
    //Submit a completed job, validating if possible.
    //If a job fails validation the server should retry on another phone.
    //If a job fails repeatedly, the whole computation should be suspended and this MUST be logged by the server.
    //If a job has the correct format but the wrong result then return JobInvalidException.
    public void submitJob(Job toSubmit) throws FormatInvalidException, JobInvalidException;
    
    //We may add information about whether each job should be run on multiple phones,
    //which would give a better guarantee of accurate results.
}
