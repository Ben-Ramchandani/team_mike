package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;


@Entity
@Table(name = "all_computation")
public class Computation {
	
	@Id
	public Long computationID;
	public String computationName;
	public String computationDescription;
	
	public int jobsLeft;
	
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
	
	
	//public logo image?
}
