package twork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import twork.JobScheduler.ScheduleJob;

import com.avaje.ebean.Ebean;

import models.Computation;
import models.Job;

public class ComputationManager {
	
	//Computations that are running
	List<Computation> activeComputations;

	public ComputationManager() {
		activeComputations = new LinkedList<Computation>();
	}
	
	
	//Rebuild from the database
	public synchronized void rebuild() {
	}
	
	public synchronized void jobCompleted(Job j) {
		
	}
	
	public synchronized void jobFailed(Job j) {
		
	}


	private synchronized void failComputation(Computation c) {
		activeComputations.remove(c);
	}


}