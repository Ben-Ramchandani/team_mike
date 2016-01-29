public interface Job {
    //The interface for Jobs returned by Computation Objects, for use on the server.
    //Methods should not block.
    
    //A unique (for this Computation) identifier associated with this job, given by the server.
    public long getJobID();
    
    //The ID of its parent computation
    public long getComputationID();
    
    //A reference to its parent computation.
    //Should be initialised by the computation object before it is handed to the server.
    public Computation getParentComputation();
    
    //Get the data to send to the client.
    //Should be specific to this job.
    //This could consist of links to other resources to be fetched over HTTP.
    public String getJobData();
    
    //Add the data returned by the phone before handing it back to it's parent Computation.
    public void addReturnData(String data) throws FormatInvalidException;
}
