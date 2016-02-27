package team_mike.server;

public class ComputationDesc {
    //This class just holds data from the DB in a convenient format.
    public long id;
    public String name;
    public String param;
    public String result = "";
    public ComputationStatus status;
}
