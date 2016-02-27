package team_mike.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleManager implements ComputationManager {

	//Computations that may still need jobs taken
	List<Computation> waitingComputations;

	//Computations that are just waiting for jobs to return
	List<Computation> exaustedComputations;

	//Number of active jobs for each computation
	Map<Computation, Integer> activeJobCount;

	DatabaseModule database;

	public SimpleManager(DatabaseModule d) {
		database = d;
		waitingComputations = new LinkedList<Computation>();
		exaustedComputations = new LinkedList<Computation>();
		activeJobCount = new HashMap<Computation, Integer>();
	}

	@Override
	public synchronized Job getJob() {
		Job j = null;
		Computation c = null;
		for(int i = 0; i < waitingComputations.size(); i++) {
			c = waitingComputations.get(i);
			j = c.getJob();
			if(j != null) {
				activeJobCount.put(c, activeJobCount.get(c) + 1);
				if(c.isExausted()) {
					waitingComputations.remove(c);
					exaustedComputations.add(c);
				}
				break;
			}
		}
		
		return j;
	}

	private synchronized void failComputation(Computation c) {
		activeJobCount.remove(c);
		waitingComputations.remove(c);
		exaustedComputations.remove(c);
		long id = c.getComputationID();
		database.setComputationFailed(id);
		database.log("Computation failed: name = " + c.getName() + ", id = " + Long.toString(id) + ".");
	}

	private void completeComputation(Computation c) {
		long id = c.getComputationID();
		activeJobCount.remove(c);
		database.computationComplete(id, c.getResult());
	}

	@Override
	public synchronized void submitJob(Job j, long phoneID) {
		Computation c = j.getParentComputation();
		boolean waiting = waitingComputations.contains(c);
		boolean exausted = exaustedComputations.contains(c);
		
		
		if(waiting || exausted) {
			try {
				c.submitJob(j);
			} catch(Exception e) {
				database.log("Job submission failed.");
				failComputation(c);
				return;
			}
			
			if(waiting && c.isExausted()) {
				waitingComputations.remove(c);
				exaustedComputations.add(c);
			}
			
		} else {
			database.log("Job submitted for non-running computation");
			return;
		}
		
		if(c.isComplete()) {
			waitingComputations.remove(c);
			exaustedComputations.remove(c);
			completeComputation(c);
			return;
		}
		
		activeJobCount.put(c, activeJobCount.get(c) - 1);
		
		if(activeJobCount.get(c) <= 0) {
			failComputation(c);
		}
	}

	@Override
	public synchronized void jobFailed(Job j) {
		Computation c = j.getParentComputation();
		failComputation(c);
	}
	
	private synchronized void addComp(Computation c) {
		if(activeJobCount.keySet().contains(c)) {
			database.log("Manager already contains given computation.");
			return;
		}
		
		if(c.isExausted()) {
			database.log("Computation reports instantly exausted: name = "
					+ c.getName() + ", id = " + Long.toString(c.getComputationID()) + ".");
			failComputation(c);
		} else {
			waitingComputations.add(c);
		}
		activeJobCount.put(c, 0);
	}
	
	@Override
	public synchronized void addComputation(Computation c) {
		long id = c.getComputationID();
		if(c.isComplete()) {
			database.log("Computation reports instantly complete: name = " + c.getName() + ", id = " + Long.toString(id) + ".");
			completeComputation(c);
		} else {
			addComp(c);
		}
	}

}
