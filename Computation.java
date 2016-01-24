public interface Computation {
    //The interface for the computation Object given to us by the client.
    //All methods:
    //MUST be thread safe.
    //MUST not block.
    
    //A unique identifier associated with the object.
    public long getComputationID();
    
    //Request a job from this computation.
    //Computation MUST NOT be complete.
    //If the Computation needs other jobs to complete first then JobNotAvailableException should be throw.
    public Job getJob() throws JobNotAvailableException;
    
    //Check whether this computation is complete.
    public boolean isComplete();
    
    //Get the result of the computation.
    //Computation MUST be complete.
    public String getResult();
    
    //Get the code for this computation.
    public ComputationCode getCode();
    
    //Get the data associated with this computation.
    //This should only be data common to all jobs this object gives.
    public String getComputationData();
    
    //Get the identifier for this computation.
    //Must be unique and unchanging.
    public long getUID();
    
    //Submit a completed job.
    //If a job fails validation the server should retry on another phone.
    //If a job fails repeatedly the whole computation should be suspended and this MUST be logged by the server.
    public void submitJob(Job toSubmit) throws FormatInvalidException;
    
    //Is there code for validating the result of a job?
    public boolean isJobValidationEnabled();
    
    //Is a finshed job correct?
    //If validation is not possible then JobValidationNotEnabledException should be thrown.
    //Computation MUST NOT be complete.
    public boolean validateJob(Job toValidate) throws JobValidationNotEnabledException;
    
    //Reset all active jobs.
    //This can be used if a server restart looses the list of active jobs.
    //If this is not possible the computation should reset.
    public void reset();
}
