package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.Model;

@Entity
@Table(name = "all_completed_computation")
public class CompletedComputation extends Model {
	@Id
	public UUID CompletedComputationID;
	public String functionName;
	public String computationDescription;
	public UUID computationID;


	public int totalJobs;

	public String input;
	
	public String output;

	public CompletedComputation(Computation c, String result) {
		functionName = c.functionName;
		computationDescription = c.computationDescription;
		computationID = c.computationID;
		totalJobs = c.jobs.size();
		input = c.input;
		output = result;
	}
}
