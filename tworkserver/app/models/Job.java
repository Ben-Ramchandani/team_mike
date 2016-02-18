package models;

import java.util.Formatter;
import java.util.UUID;

import javax.persistence.*;

import twork.MyLogger;

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
	
	public UUID inputDataID;
	
	private String functionCodeName;
	//address of the function class.
	//these are simply on the file system, no database.
	
	public UUID outputDataID;
	
	//Slated for removal
	public boolean failed;
	
	public Job(Computation parent, String description,
				UUID input, String function) {
		
		parentComputation = parent;
		computationID = parent.computationID;
		inputDataID = input;
		functionCodeName = function;
		outputDataID = Device.NULL_UUID;
		failed = false;
	}

	
	
	public String export() {
		String result = "{}";
		try {
    	StringBuilder sb = new StringBuilder();
    	Formatter formatter = new Formatter(sb);
    	
    	formatter.format("{\n" +
    					 "\"computation-id\": %d,\n" +
    					 "\"job-id\" : %d,\n" +
    					 "\"function-class\" : \"%s\",\n" +
    					 "\"job-description\": \"%s\"\n}", computationID.getLeastSignificantBits(), jobID.getLeastSignificantBits(), functionCodeName, jobDescription);
    	
    	result = formatter.out().toString();
    	formatter.close();
		} catch(Exception e) {
			e.printStackTrace();
			MyLogger.warn("job failed to export");
		}
		
    	return result;
    }
	
}