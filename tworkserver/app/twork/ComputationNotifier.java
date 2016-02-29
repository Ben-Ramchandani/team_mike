package twork;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import models.CustomerComputation;

public class ComputationNotifier {
	
	public static Map<UUID, CustomerComputation> computations = Collections.synchronizedMap(new HashMap<UUID, CustomerComputation>());
	public static Map<UUID, Boolean> pollingFlags = Collections.synchronizedMap(new HashMap<UUID, Boolean>());
	
	//Hack to let us stop these threads
	private static long version = 0;
	//Count threads
	private static int threadCount = 0;
	
	public static void reset() {
		version++;
	}
	
	public static String track(CustomerComputation c) {
		if(threadCount >= 3) {
			return "System is too busy to handle your request";
		} else {
			threadCount++;
		}
		
		if (c.status == CustomerComputation.COMPLETE) 
			return c.output;
		
		
		computations.put(c.customerComputationID,c);
		pollingFlags.put(c.customerComputationID,true);
		
		long v = version;
	
		while (pollingFlags.get(c.customerComputationID) == true){
			if(v != version) {
				threadCount--;
				return "System has been reset";
			}
		}
		
		pollingFlags.remove(c.customerComputationID);
		c = ComputationManager.getInstance().getCustomerComputation(c.customerComputationID);
		threadCount--;
        return c.output;
	}
	
	public static void finished(UUID cid) {
		if (computations.get(cid) != null) {
			pollingFlags.put(cid,false);
		}
	}

}
