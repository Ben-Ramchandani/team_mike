package computations;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import com.avaje.ebean.Ebean;

import models.Computation;
import models.Data;
import models.Job;
import twork.Device;
import twork.MyLogger;

public class MapComputation implements BasicComputationGenerator {

	private String functionName = "EdgeDetect";
	
	public MapComputation(String name) {
		functionName = name;
	}
	
	@Override
	public UUID generateComputation(String input) {
		
		String[] data = input.split("\n");
		
		Computation c = new Computation(functionName, "Image Processing");
		
		Ebean.beginTransaction();
		
		try {
			c.save(); //Generate UUID
			System.out.printf("computation id in map: %s\n",c.computationID);
			long jobs = data.length;
				for (int i = 0; i < jobs; i++) {
				Job j = new Job(c,"Image Processing",UUID.fromString(data[i]),functionName);
				j.save();
				c.jobs.add(j);
			}
		
			c.setJobsLeft(c.jobs.size());
			c.setInput(input);
			c.update();
			Ebean.commitTransaction();
		} finally {
			Ebean.endTransaction();
		}
		// ### End transaction ###
		System.out.printf("Number of jobs in map: %d\n%s\n",c.jobsLeft,c.functionName);
		return c.computationID;
	}
	
	@Override
	//TODO
	public String getResult(UUID computationID) {
		Computation c = Ebean.find(Computation.class, computationID);
		
		for(Job j : c.jobs) {
			UUID dataID = j.outputDataID;
			
			if(!dataID.equals(Device.NULL_UUID)) {
				//TODO: dependent on the data class.
				Data d = Ebean.find(Data.class, dataID);
				
				if(!(d == null)) {
					if (d.type == Data.TYPE_FILE) {
						
					}
				}
			}
		}
		return "How am I supposed to return this?";
		
	}

}
