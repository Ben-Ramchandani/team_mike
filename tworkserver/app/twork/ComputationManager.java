package twork;

import java.util.LinkedList;
import java.util.List;

import models.Computation;

public class ComputationManager {
	
	//Computations that are running
	List<Computation> activeComputations;

	public ComputationManager() {
		activeComputations = new LinkedList<Computation>();
	}


	private synchronized void failComputation(Computation c) {
		activeComputations.remove(c);
	}


}