package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;

import play.api.libs.json.Json;
import play.data.format.*;
import play.data.validation.*;

@Entity	
@Table(name = "all_jobs")
public class Job extends Model {
	@Id
	public Long jobID;
	public String jobName;
	
	@ManyToOne
	public Long computationID;
	
	public Long inputDataID;
	
	public String functionID;
	//address of the function class.
	//these are simply on the file system, no database.
	
	public Long outputDataID;
	
	public Integer retries;
	
	
	public String export() {
    	StringBuilder sb = new StringBuilder();
    	Formatter formatter = new Formatter(sb);
    	
    	formatter.format("{           \n        " +
    					 "\"computation-id\": %l,\n" +
    					 "\"job-id\" : %l,\n       " +
    					 "\"function-class\" : %s,\n" +
    					 "\"job-description\": %s,\n", computationID, jobID, functionID, jobName);
    	return formatter.toString();
    }
    
}