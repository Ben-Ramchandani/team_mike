package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "all_computation")
public class Computation {


	@Id
	public UUID computationID;
	public String computationName;
	public String computationDescription;
	
	public boolean failed = false;
	
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
	
	public String data;
	
	
	public void parallelize() {
		//Parallelizer p = new Parallelize
		
		//here I create the jobs
	}
	
	public void getinput() {
		//here I get the input from wherever the user adds it too (late feature)
	}
	
	
	public Long logoImageID;
}
