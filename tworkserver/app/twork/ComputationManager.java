package twork;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.CustomerComputation;
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
			} else {
				completeComputation(c.computationID);
			}

		}
	}

	public int getNumberOfComputations() {
		return jobsRemaining.size();
	}


	public synchronized void jobCompleted(UUID jID) {
		Job j = Ebean.find(Job.class, jID);
		if(j == null) {
			MyLogger.log("Job completed does not exist.");
			return;
		}
		

		UUID computationID = j.computationID;
		Integer jrInteger = jobsRemaining.get(computationID);
		if(jrInteger == null) {
			MyLogger.log("Job completed for computation not in manager.");
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
			MyLogger.log("Job failed that does not exist.");
			return;
		}

		UUID computationID = j.computationID;
		if(jobsRemaining.get(computationID) == null) {
			MyLogger.log("Job failed for computation that is not in manager.");
		} else {
			failComputation(computationID);
		}
	}

	public synchronized void runCustomerComputation(CustomerComputation cc) {
		//TODO: This shouldn't be synchronized in the manager
		//TODO: should all be in try/catch
		BasicComputationGenerator g = FunctionManager.getInstance().getBasicComputationGenerator(cc.functionName);
		if(g == null) {
			MyLogger.warn("No generator for added customer computation, failing.");
			cc.fail();
			return;
		}

		//TODO: Move transaction round whole thing
		UUID id = g.generateComputation(cc.input);


		//Do some checks and add to the job count map
		Computation c = Ebean.find(Computation.class, id);
		if(c == null) {
			MyLogger.log("Computation generator failed.");
		} else {
			cc.runComputation(c);
			
			int jobsLeft = c.jobsLeft;
			if(jobsLeft > 0) {
				jobsRemaining.put(id, c.jobsLeft);
				JobScheduler.getInstance().update();
			} else {
				MyLogger.log("Computation added has no jobs. Will attempt to complete.");
				completeComputation(id);
			}
		}
	}

	@Deprecated
	public synchronized void addBasicComputation(BasicComputationGenerator g, String input) {
		//TODO: This shouldn't be synchronized in the manager
		UUID id = g.generateComputation(input);

		//Do some checks and add to the job count map
		Computation c = Ebean.find(Computation.class, id);
		if(c == null) {
			MyLogger.log("Computation generator failed.");
		} else {
			int jobsLeft = c.jobsLeft;
			if(jobsLeft > 0) {
				jobsRemaining.put(id, c.jobsLeft);
				JobScheduler.getInstance().update();
			} else {
				MyLogger.log("Computation added has no jobs. Will attempt to complete.");
				completeComputation(id);
			}
		}
	}

	public List<CustomerComputation> getCustomerComputations() {
		List<CustomerComputation> list = Ebean.find(CustomerComputation.class).findList();
		Collections.sort(list);
		return list;
	}
	
	
	//Computations by name.
	//may return empty list.
	public List<CustomerComputation> getComputationsByCustomerName(String name) {
		List<CustomerComputation> all = getCustomerComputations();
		List<CustomerComputation> ret = new LinkedList<CustomerComputation>();
		for(CustomerComputation cc : all) {
			if(cc.customerName.equals(name)) {
				ret.add(cc);
			}
		}
		return ret;
	}

	private synchronized void completeComputation(UUID computationID) {
		jobsRemaining.remove(computationID);
		Computation comp = Ebean.find(Computation.class, computationID);
		if(comp == null) {
			MyLogger.log("Non existent computation completed, ignoring.");
		} else {

			//BAD
			comp.jobsLeft = 0;
			comp.update();
			/*
			 * Have different cases for the different types of computations.
			 * Just have this one for now.
			 */
			BasicComputationGenerator gen = FunctionManager.getInstance().getBasicComputationGenerator(comp.functionName);
			//This should be in a different thread really.
			String result = gen.getResult(computationID);
			CustomerComputation cc;

			cc = getCustomerComputation(comp);

			cc.addResult(result);
			cc.update();
			comp.delete();
			
			
			//Add notification here
			
			
			MyLogger.log("Computation completed.");
			MyLogger.log(cc.toString());
		}
	}

	@SuppressWarnings("unused")
	private synchronized void abortComputation(Computation c) {
		jobsRemaining.remove(c.computationID);
		try {
			c.delete();
			MyLogger.warn("Computation aborted");
		} catch(Throwable e) {
			MyLogger.critical("An attempt was made to abort an invalid computation, but it failed.");
			e.printStackTrace();
		}
	}

	private CustomerComputation getCustomerComputation(Computation c) {
		CustomerComputation cc;
		if(c.customerComputationID == null) {
			MyLogger.warn("No customer computation found for computation (UUID was null). Will generate one.");
			cc = new CustomerComputation(c, "");
			c.customerComputationID = cc.CustomerComputationID;
		}

		cc = Ebean.find(CustomerComputation.class, c.customerComputationID);

		if(cc == null) {
			MyLogger.warn("No customer computation found for completing computation (not in database). Will generate one.");
			cc = new CustomerComputation(c, "");
		}

		return cc;
	}


	private synchronized void failComputation(UUID computationID) {
		jobsRemaining.remove(computationID);
		Computation comp = Ebean.find(Computation.class, computationID);
		if(comp == null) {
			MyLogger.log("Non existent computation failed.");
		} else {
			MyLogger.log("Failing computation: A job could not be completed.\nComputationID: "
					+ computationID.toString() + "\nfuncitonName: " + (comp.functionName == null ? "null" : comp.functionName) + ".");

			CustomerComputation cc = getCustomerComputation(comp);
			Ebean.beginTransaction();
			try {
				cc.fail();
				//Delete cascades through to jobs.
				comp.delete();
				Ebean.commitTransaction();
			} finally {
				Ebean.endTransaction();
			}
			JobScheduler.getInstance().update();
		}
	}
}