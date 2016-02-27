package team_mike.server;

public interface JobData {
    //The interface for Jobs returned by Computation Objects.
	//This is the read-only version that is sent to the network module.
    //Methods should not block.
    
    //A unique (for this Computation) identifier associated with this job, given by the server.
    public long getJobID();
    //Unique string identifier.
    public String getJobName();
    
    //The ID of its parent computation
    public long getComputationID();
    
    //Get the data to send to the client.
    //Should be specific to this job.
    //This could consist of links to other resources to be fetched over HTTP.
    public String getJobData();
    
    //Get the title to be displayed on the UI
    public String getDescTitle();
    
    //Get a description to be displayed in the UI
    public String getDescString();
}
