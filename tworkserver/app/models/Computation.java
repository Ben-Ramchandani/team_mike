package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;


@Entity
@Table(name = "all_computation")
public class Computation extends Model {

	@Id
	public UUID computationID;
	public int getJobsLeft() {
		return jobsLeft;
	}

	public void setJobsLeft(int jobsLeft) {
		this.jobsLeft = jobsLeft;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String functionName;
	//Purely descriptive name
	public String computationName;
	

	
	public Boolean failed;
	public Boolean running;
	//Has the result of this computation been collected?
	public Boolean completed;
	
	//UUID of the customer computation that spawned this (can be NULL_UUID).
	public UUID customerComputationID;
	
	public Integer jobsLeft;
	
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy="parentComputation")
	public List<Job> jobs;
	
	@Column(length=1024)
	public String input;
	
	/*
	 * Cases for computations
	 * 
	 * 
	 * The user uploads a single String (example - prime)
	 * The user uploads all the files (this is gonna be supported next week when we do the image processing example)
	 * The user uploads only one document/image which we know how to split (optional)
	 * 
	 * 
	*/
	
	public Computation(String function, String name) {
		functionName = function;
		computationName = name;
		jobs = new ArrayList<Job>();
	}
		
	public Long logoImageID;
}
