package computations;

import java.util.UUID;

import models.Computation;
import models.Job;
import twork.MyLogger;

import com.avaje.ebean.Ebean;

public class MapComputation implements BasicComputationGenerator {

	private String functionName;

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
			
			long jobs = data.length;
			for (int i = 0; i < jobs; i++) {
				UUID dataID;
				try {
					dataID = UUID.fromString(data[i]);
				} catch(IllegalArgumentException e) {
					MyLogger.warn("MapComputation.generateComputation: Unable to parse UUID from input");
					continue;
				}
				Job j = new Job(c, "Image Processing", dataID, functionName);
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
		MyLogger.log("New MapComputation generated.");
		MyLogger.log("Number of jobs in map: " + c.jobsLeft);
		return c.computationID;
	}

	@Override
	public String getResult(UUID computationID) {
		/*
		 * Nothing to do - result is returned on a per-job basis.
		 */
		return "MapComputation completed";
	}

}
