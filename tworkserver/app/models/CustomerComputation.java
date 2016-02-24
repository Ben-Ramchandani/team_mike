package models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import twork.ComputationManager;

import com.avaje.ebean.Model;

/*
 * This is created when a customer adds a computation.
 * Having it separate makes it easy to store lots of completed computations without
 * cluttering the rest of the database with dead computations and jobs.
 */

@Entity
@Table(name = "all_completed_computation")
public class CustomerComputation extends Model implements Comparable<CustomerComputation> {
	@Id
	public UUID customerComputationID;
	//Name of the function
	public String functionName;
	//Purely descriptive names
	public String computationName;
	@Column(length=1024)
	public String computationDescription;
	//id of the running counterpart if it exists
	//0 - waiting to run; 1 - running; 2 - complete; 3 - failed
	public int status;
	public UUID computationID;
	public String customerName;
	public long timeStamp;
	
	public int compareTo(CustomerComputation cc) {
		if(this.timeStamp < cc.timeStamp) {
			return -1;
		} else if(this.timeStamp > cc.timeStamp){
			return 1;
		} else {
			return 0;
		}
	}
	
	//Status states
	@Transient
	public static final int WAITING = 0;
	@Transient
	public static final int RUNNING = 1;
	@Transient
	public static final int COMPLETE = 2;
	@Transient
	public static final int FAILED = 3;


	public int totalJobs;

	@Column(length=1024)
	public String input;
	
	@Column(length=1024)
	public String output;
	

	//Main constructor
	public CustomerComputation(String customerName, String computationName, String desc, String function, String input) {
		functionName = function;
		this.computationName = computationName;
		this.customerName = customerName;
		computationDescription = desc;
		computationID = null;
		totalJobs = 0;
		this.input = input;
		output = "";
		status = WAITING;
		timeStamp = (new Date()).getTime();
		//Generate ID
		this.save();
		ComputationManager.getInstance().addCustomerComputation(this);
	}
	
	
	public String toString() {
		return "Customer Computation:\nCustomer Name: \"" + customerName + "\", input: \"" + input + "\", output: \"" + output + "\".";
	}
	
	/*
	 * IMPORTANT:
	 * None of the following methods should be called outside of the computation manager,
	 * it may leave the database in an inconsistent state.
	 */
	
	public void runComputation(Computation c) {
		computationID = c.computationID;
		totalJobs = c.jobs.size();
		status = RUNNING;
		c.customerComputationID = this.customerComputationID;
		c.update();
		this.update();		
	}
	
	//To allow generation for compatibility
	public CustomerComputation(Computation c, String description) {
		functionName = c.functionName;
		computationName = c.computationName;
		customerName = "";
		computationDescription = description;
		computationID = c.computationID;
		totalJobs = c.jobs.size();
		input = c.input;
		output = "";
		status = RUNNING;
		timeStamp = (new Date()).getTime();
		this.save();
		ComputationManager.getInstance().addCustomerComputation(this);
		c.customerComputationID = this.customerComputationID;
	}
	
	
	public void addResult(String result) {
		output = result;
		status = COMPLETE;
		this.update();
	}
	
	public void fail() {
		status = FAILED;
		this.update();
	}
}
