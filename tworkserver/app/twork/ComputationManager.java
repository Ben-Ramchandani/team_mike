package twork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.CompletedComputation;
import models.Computation;
import models.Job;

import com.avaje.ebean.Ebean;
import computations.BasicComputationGenerator;

//Stores a list of active computation IDs in memory.
public class ComputationManager {
	
	//Singleton
	//TODO:
	public static ComputationManager instance;
	
	public static ComputationManager getInstance() {
		if(instance == null) {
			instance = new ComputationManager();
		}
		return instance;
	}

	//Jobs remaining for running computations
	Map<UUID, Integer> jobsRemaining;
	
	//Could keep a in memory map from jobID to ComputationID

	private ComputationManager() {
		rebuild_TEST();
	}


	//Rebuild from the database
	public synchronized void rebuild_TEST() {
		jobsRemaining = new HashMap<UUID, Integer>();
		List<Computation> computations = Ebean.find(Computation.class).findList();
		Iterator<Computation> it = computations.iterator();

		while(it.hasNext()) {
			Computation c = it.next();
			if(c.failed) {
				Logger.log("Failed computaiton found, removing.\nName was " + (c.functionName == null ? "null" : c.functionName) + ".");
				c.delete();
			} else if(c.completed) {
				Logger.log("Completed computaiton found, removing.\nName was " + (c.functionName == null ? "null" : c.functionName) + ".");
				c.delete();
			} else if(c.running){
				// # This is the only case that should ever happen #
				//Count the number of jobs the computation have that aren't complete.

				c.jobsLeft = 0;
				Iterator<Job> jobs = c.jobs.iterator();

				//TODO: interaction with Data()
				while(jobs.hasNext()) {
					Job j = jobs.next();
					if(j.outputDataID.equals(Device.NULL_UUID)) {
						c.jobsLeft++;
					}
				}
				
				c.update();

				if(c.jobsLeft > 0) {
					jobsRemaining.put(c.computationID, c.jobsLeft);
					// # End case #
				} else {
					completeComputation(c.computationID);
				}
				
				
			} else {//c was never set to run
				Logger.log("Non-started computaiton found, removing.\nName was " + (c.functionName == null ? "null" : c.functionName) + ".");
				c.delete();
			}
		}
	}
	
	public int getNumberOfComputations() {
		return jobsRemaining.size();
	}

	
	public synchronized void jobCompleted(UUID jID) {
		Job j = Ebean.find(Job.class, jID);
		if(j == null) {
			Logger.log("Job completed does not exist.");
			return;
		}
		
		UUID computationID = j.computationID;
		Integer jrInteger = jobsRemaining.get(computationID);
		if(jrInteger == null) {
			Logger.log("Job completed for computation not in manager.");
			return;
		}
		int jr = jrInteger;
		jr -= 1;
		if(jr == 0) {
			completeComputation(computationID);
		} else {
			jobsRemaining.put(computationID, jr);
		}
	}

	public synchronized void jobFailed(UUID jID) {
		Job j = Ebean.find(Job.class, jID);
		if(j == null) {
			Logger.log("Job failed that does not exist.");
			return;
		}
		
		UUID computationID = j.computationID;
		if(jobsRemaining.get(computationID) == null) {
			Logger.log("Job failed for computation that is not in manager.");
		} else {
			failComputation(computationID);
		}
	}

	public synchronized void addBasicComputation(BasicComputationGenerator g, String input) {
		//TODO: This shouldn't be synchronized in the manager
		UUID id = g.generateComputation(input);
		
		//Do some checks and add to the job count map
		Computation c = Ebean.find(Computation.class, id);
		if(c == null) {
			Logger.log("Computation generator failed.");
		} else {
			int jobsLeft = c.jobsLeft;
			if(jobsLeft > 0) {
				jobsRemaining.put(id, c.jobsLeft);
				JobScheduler.getInstance().update();
			} else {
				Logger.log("Computation added has no jobs. Will attempt to complete.");
				completeComputation(id);
			}
		}
	}
	
	public List<CompletedComputation> getCompletedComputations() {
		return Ebean.find(CompletedComputation.class).findList();
	}
	
	private synchronized void completeComputation(UUID computationID) {
		jobsRemaining.remove(computationID);
		Computation comp = Ebean.find(Computation.class, computationID);
		if(comp == null) {
			Logger.log("Non existent computation completed, ignoring.");
		} else {
			comp.jobsLeft = 0;
			comp.running = false;
			comp.save();
			/*
			 * Have different cases for the different types of computations.
			 * Just have this one for now.
			 */
			BasicComputationGenerator gen= FunctionManager.getBasicComputationGenerator(comp.functionName);
			//This should be in a different thread really.
			String result = gen.getResult(computationID);
			
			CompletedComputation cc = new CompletedComputation(comp, result);
			cc.save();
			comp.delete();
			Logger.log("Computation completed.");
		}
	}


	private synchronized void failComputation(UUID computationID) {
		jobsRemaining.remove(computationID);
		Computation comp = Ebean.find(Computation.class, computationID);
		if(comp == null) {
			Logger.log("Non existent computation failed.");
		} else {
			Logger.log("Failing computation: A job could not be completed.\nComputationID: "
					+ computationID.toString() + "\nfuncitonName: " + (comp.functionName == null ? "null" : comp.functionName) + ".");
			//Delete cascades through to jobs.
			comp.delete();
			JobScheduler.getInstance().update();
		}
	}
}