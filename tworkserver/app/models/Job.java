package models;

import java.util.Formatter;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.*;

import twork.Device;

import com.avaje.ebean.Model;

@Entity	
@Table(name = "all_jobs")
public class Job extends Model {
	@Id
	public UUID jobID;
	public String jobDescription;
	
	@ManyToOne
	public Computation parentComputation;
	
	public UUID computationID;
	
	public UUID intputDataID;
	
	public String functionID;
	//address of the function class.
	//these are simply on the file system, no database.
	
	public UUID outputDataID;
	
	//Slated for removal
	public boolean failed;
	
	public Job(Computation parent, String description,
				UUID input, String function) {
		
		parentComputation = parent;
		computationID = parent.computationID;
		intputDataID = input;
		functionID = function;
		outputDataID = Device.NULL_UUID;
		failed = false;
	}

	
	
	public String export() {
    	StringBuilder sb = new StringBuilder();
    	Formatter formatter = new Formatter(sb);
    	
    	formatter.format("{           \n        " +
    					 "\"computation-id\": %l,\n" +
    					 "\"job-id\" : %l,\n       " +
    					 "\"function-class\" : %s,\n" +
    					 "\"job-description\": %s,\n", computationID, jobID.getLeastSignificantBits(), functionID, jobDescription);
    	
    	String result = formatter.out().toString();
    	formatter.close();
    	return result;
    }
	
	 public static Finder<UUID, Job> find = new Finder<UUID,Job>(Job.class);    
}