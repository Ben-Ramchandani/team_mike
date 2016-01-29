//A Java object to hold the code for a computation.
public interface ComputationCode {
    //This method contains the code to complete the job.
    //It should return the result of the job.
    public String run(String computationData, String jobData);
}