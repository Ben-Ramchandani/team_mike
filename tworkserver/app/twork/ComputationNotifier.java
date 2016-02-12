package twork;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import models.CustomerComputation;

public class ComputationNotifier {

	public static ComputationNotifier instance;
	
	
	public static ComputationNotifier getInstance() {
		if(instance == null) {
			instance = new ComputationNotifier();
		}
		return instance;
	}
	
	public static Map<UUID, CustomerComputation> computations = Collections.synchronizedMap(new HashMap<UUID, CustomerComputation>());
	public static Map<UUID, Boolean> pollingFlags = Collections.synchronizedMap(new HashMap<UUID, Boolean>());
	
	public static String track(CustomerComputation c) {
		if (c.status == CustomerComputation.COMPLETE) 
			return c.output;
		
		computations.put(c.CustomerComputationID,c);
		pollingFlags.put(c.CustomerComputationID,true);
		/* This does not work
		try {
			c.wait();
		} catch (InterruptedException e) {
			System.err.println("failed to sleep controller thread");
			return null;
		}*/
		int i = 0;
		while (pollingFlags.get(c.CustomerComputationID) == true){
			i++;
		}
		
		c = ComputationManager.getInstance().getCustomerComputation(c.CustomerComputationID);
        return c.output;
	}
	
	public static void finished(UUID cid) {
		if (computations.get(cid) == null) {
			//I don't know what to do
		}
		else {
//			computations.get(cid).notify();
			pollingFlags.put(cid,false);
		}
	}

}
