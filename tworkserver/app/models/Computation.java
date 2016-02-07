package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;


@Entity
@Table(name = "all_computation")
public class Computation extends Model {

	public static final int STATE_FAILED = 1;
	public static final int STATE_RUNNING = 2;
	public static final int STATE_COMPLETED = 3;

	@Id
	public UUID computationID;
	public String computationName;
	public String computationDescription;
	
	
	public boolean failed;
	public boolean running;
	//Has the result of this computation been collected?
	public boolean completed;
	//A single field should suffice here?
	public int state;
	
	public int jobsLeft;
	
	@OneToMany
	public List<Job> jobs;
	
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
	
	public Computation(String name, String desc) {
		computationName = name;
		computationDescription = desc;
		failed = completed = running = false;
	}
	
	
	//I'll have a separate class with the parrallelize stuff in, it's too different between computations.

	
	public void getinput() {
		//here I get the input from wherever the user adds it too (late feature)
	}
	
	
	public Long logoImageID;
}
