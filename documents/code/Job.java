package team_mike.server;

public interface Job extends JobData {
    //The interface for Jobs returned by Computation Objects, for use on the server.
    //Methods should not block.
    
    //A unique (for this Computation) identifier associated with this job, given by the server.
    public long getJobID();
    //Unique string identifier.
    public String getJobName();
    
    //The ID of its parent computation
    public long getComputationID();
    
    //A reference to its parent computation.
    //Should be initialised by the computation object before it is handed to the server.
    public Computation getParentComputation();
    
    //Get the data to send to the client.
    //Should be specific to this job.
    //This could consist of links to other resources to be fetched over HTTP.
    public String getJobData();
    
    //Does this job need extra data?
    public boolean hasExtraJobData();
    
    //Add the data returned by the phone before handing it back to it's parent Computation.
    public void addReturnData(String data) throws FormatInvalidException;
    
    //Get the title to be displayed on the UI
    public String getDescTitle();
    
    //Get a description to be displayed in the UI
    public String getDescString();
}
