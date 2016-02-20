package twork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import models.CustomerComputation;
import models.Computation;
import models.Job;

import com.avaje.ebean.Ebean;
import computations.BasicComputationGenerator;

//Stores a list of active computation IDs in memory.
public class ComputationManager {

	//Singleton
	public static ComputationManager instance;
	
	
	public static ComputationManager getInstance() {
		if(instance == null) {
			instance = new ComputationManager();
		}
		return instance;
	}

	//Jobs remaining for running computations
	Map<UUID, Integer> jobsRemaining;

	//Could keep an in memory map from jobID to ComputationID
	
	//Map CustCompID -> CustComp
	Map<UUID, CustomerComputation> customerComputations;

	private ComputationManager() {
		rebuild_TEST();
	}

	private void rebuildCustomerComputationMap() {
		Map<UUID, CustomerComputation> newCustomerComputations = new ConcurrentHashMap<UUID, CustomerComputation>();
		List<CustomerComputation> ccs = Ebean.find(CustomerComputation.class).findList();
		Iterator<CustomerComputation> it = ccs.iterator();
		
		while(it.hasNext()) {
			CustomerComputation current = it.next();
			newCustomerComputations.put(current.customerComputationID, current);
			
			//TODO: build map from customer name to computation list
		}
		
		synchronized(this) {
			customerComputations = newCustomerComputations;
		}
	}

	//Complete rebuild from the database (slow)
	public synchronized void rebuild_TEST() {
		rebuildCustomerComputationMap();
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
		runCustomerComputation(cc.customerComputationID);
	}

	public synchronized void runCustomerComputation(UUID customerComputationID) {
		//TODO: This shouldn't be synchronized in the manager
		//TODO: should all be in try/catch
		
		CustomerComputation cc = getCustomerComputation(customerComputationID);
		
		if(cc == null) {
			MyLogger.warn("ComputationManager.runCustomerComputation: Customer computation with this ID does not exist in manager");
			return;
		}
		
		BasicComputationGenerator g = FunctionManager.getInstance().getBasicComputationGenerator(cc.functionName);
		if(g == null) {
			MyLogger.warn("No generator for added customer computation, failing.");
			cc.fail();
			return;
		}

		//TODO: Move transaction round whole thing
		MyLogger.log("New computation added.");
		UUID id = g.generateComputation(cc.input);


		//Do some checks and add to the job count map
		Computation c = Ebean.find(Computation.class, id);
		
		if(c == null) {
			MyLogger.warn("Computation generator failed.");
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

	//Used only for testing
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

	
	//DO NOT USE
	//Called only by the CustomerComputation constructor
	public void addCustomerComputation(CustomerComputation c) {
		customerComputations.put(c.customerComputationID, c);
	}
	
	
	public List<CustomerComputation> getCustomerComputations() {
		List<CustomerComputation> list = new ArrayList<CustomerComputation>(customerComputations.values());
		return list;
	}
	
	
	public CustomerComputation getCustomerComputation(UUID cid) {
		CustomerComputation c = customerComputations.get(cid);
		return c;
	}
	
	
	//Computations by name.
	//May return an empty list.
	public List<CustomerComputation> getComputationsByCustomerName(String name) {
		List<CustomerComputation> all = getCustomerComputations();
		List<CustomerComputation> ret = new LinkedList<CustomerComputation>();
		for(CustomerComputation cc : all) {
			if(cc.customerName.equals(name)) {
				ret.add(cc);
			}
		}
		Collections.sort(ret);
		return ret;
	}

	private synchronized void completeComputation(UUID computationID) {
		jobsRemaining.remove(computationID);
		Computation comp = Ebean.find(Computation.class, computationID);
		if(comp == null) {
			MyLogger.log("Non existent computation completed, ignoring.");
		} else {

			/*
			 * Have different cases for the different types of computations.
			 * Just have this one for now.
			 */
			BasicComputationGenerator gen = FunctionManager.getInstance().getBasicComputationGenerator(comp.functionName);
			
			
			//TODO: This should all be in a different thread really.
			String result = gen.getResult(computationID);
			
			CustomerComputation cc = getCustomerComputation(comp);

			cc.addResult(result);
			comp.delete();
			
			//Notify
			ComputationNotifier.finished(cc.customerComputationID);
			
			MyLogger.log("Computation completed.");
			MyLogger.log(cc.toString());
		}
	}

	

	private CustomerComputation getCustomerComputation(Computation c) {
		CustomerComputation cc;
		
		if(c.customerComputationID == null) {
			MyLogger.warn("No customer computation found for computation (UUID was null). Will generate one.");
			cc = new CustomerComputation(c, "Automatically generated by ComputationManager");
		}

		cc = getCustomerComputation(c.customerComputationID);

		if(cc == null) {
			cc = Ebean.find(CustomerComputation.class, c.customerComputationID);
			if(cc == null) {
				MyLogger.warn("No customer computation found for completing computation (not in database). Will generate one.");
				cc = new CustomerComputation(c, "Automatically generated by ComputationManager");
			} else {
				MyLogger.critical("Customer computation was in database but not manager.");
				addCustomerComputation(cc);
			}
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