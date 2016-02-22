package twork;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import models.CustomerComputation;

public abstract class ComputationNotifier {
	
	public static Map<UUID, CustomerComputation> computations = Collections.synchronizedMap(new HashMap<UUID, CustomerComputation>());
	public static Map<UUID, Boolean> pollingFlags = Collections.synchronizedMap(new HashMap<UUID, Boolean>());
	
	public static String track(CustomerComputation c) {
		if (c.status == CustomerComputation.COMPLETE) 
			return c.output;
		
		computations.put(c.customerComputationID,c);
		pollingFlags.put(c.customerComputationID,true);
	
		int i = 0;
		while (pollingFlags.get(c.customerComputationID) == true){
			i++;
		}
		
		c = ComputationManager.getInstance().getCustomerComputation(c.customerComputationID);
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
