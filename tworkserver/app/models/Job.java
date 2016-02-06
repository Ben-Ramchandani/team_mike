package models;

import java.util.Formatter;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.avaje.ebean.Model;

@Entity	
@Table(name = "all_jobs")
public class Job extends Model {
	@Id
	public UUID jobID;
	public String jobDescription;
	
	@ManyToOne
	public Computation parentComputation;
	
	public Long computationID;
	
	@OneToOne(cascade=CascadeType.ALL)
	public Data intputData;
	
	public String functionID;
	//address of the function class.
	//these are simply on the file system, no database.
	
	@OneToOne(cascade=CascadeType.ALL)
	public Data outputData;

	
	
	public String export() {
    	StringBuilder sb = new StringBuilder();
    	Formatter formatter = new Formatter(sb);
    	
    	formatter.format("{           \n        " +
    					 "\"computation-id\": %l,\n" +
    					 "\"job-id\" : %l,\n       " +
    					 "\"function-class\" : %s,\n" +
    					 "\"job-description\": %s,\n", computationID, jobID, functionID, jobDescription);
    	
    	String result = formatter.out().toString();
    	formatter.close();
    	return result;
    }
	
	 public static Finder<Long, Job> find = new Finder<Long,Job>(Job.class);
    
}