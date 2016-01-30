public interface DatabaseModule {
    //This class manages the PostgreSQL database and any other non-volatile storage
    
    //The idea is that as soon as a computation is recieved all its data is written to the file system or this database.
    //So it's name and parameters are written to the DB, the code and other large files are written to the FS.
    //That way all computations can be reconstructed on server restart.
    
    //Computation names should be the name of the .class file describing it.
    //The ComputationTemplate/Fuctions module can construct a computation from its name and param string
    //Any binary data (pictures etc.) will be stored separately
    
    //Tables are as follows:
    /*
     * Computation:
     * ID : long      #Unique to this computation
     * Name : string  #Unique to this computation
     * param : String #Argument used to construct computation.
     * result: String #Result of computation if it is finished
     * Status: enum   #One of COMPLETE, ACTIVE, WAITING, FAILED
     */
     
     //Jobs are written here once complete, as a record.
     /*
      * Completed jobs:
      * jobID : long
      * jobName : String
      * phoneID : long      #The phone that completed this job
      */
    
    
    //Append this string onto the log, along with a timestamp.
    //This can just be a text file.
    void log(String s);
    
    //Give out new UIDs
    long giveUniqueID();
    
    
    //Manage the contents of the DB
    public void addComputation(String name, long ID, String param);
    
    public void setComputationActive(long ID);
    
    public void setComputationFailed(long ID);
    
    public void computationComplete(long ID, string result);
    
    public List<long> getComputationIDList();
    
    public CopmutationDescr getComputation(long ID); //ComputationDesc is an object with the properties in the DB table
    
    public void removeCopmutation(long ID);
    
    public void addCompletedJob(Job j, long phoneID);
    
    public long getPhoneID(long jobID);
    public long getPhoneID(String jobName);
    
    public List<long> getJobsByPhone(long phoneID);
}