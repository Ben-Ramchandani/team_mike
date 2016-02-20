package computations;

import java.util.Scanner;
import java.util.UUID;

import models.Computation;
import models.Data;
import models.Device;
import models.Job;
import twork.MyLogger;

import com.avaje.ebean.Ebean;

//A simple computation that tries to find out if a number is prime.

public class PrimeComputation implements BasicComputationGenerator {

	private final String functionName = "PrimeComputationCode";


	@Override
	public UUID generateComputation(String input) {
		long prime;


		try {
			Scanner s = new Scanner(input);
			prime = s.nextLong();
			s.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("PrimeComputation: generation input invalid, expected \"long\"");
		}

		
		Computation c = new Computation(functionName, "Prime computation");


		// ### Start transaction ###
		Ebean.beginTransaction();
		try {
			c.save(); //Generate UUID

			//Generate jobs
			String primeString = Long.toString(prime);
			long currentStart = 2L;
			//Make about 10 jobs
			long numPerJob = prime > 10 ? prime/10 : 1;
			long currentEnd = currentStart + numPerJob;
			long stopAt = prime - 1;

			while((currentEnd) <= stopAt) {
				String jobInput = primeString + " " + Long.toString(currentStart) + " " + Long.toString(currentEnd);
				
				UUID dataID = Data.storeString(jobInput);

				Job j = new Job(c, "Prime job", dataID, functionName);
				j.save();
				c.jobs.add(j);

				currentStart = currentEnd;
				currentEnd += numPerJob;
			}


			c.setJobsLeft(c.jobs.size());
			c.setInput(input);
			c.update();


			Ebean.commitTransaction();
		} finally {
			Ebean.endTransaction();
		}
		c = Ebean.find(Computation.class, c.computationID);
		// ### End transaction ###
		return c.computationID;
	}

	public String getResult(UUID computationID) {
		long prime;
		Computation c = Ebean.find(Computation.class, computationID);
		if(c == null) {
			return "Error: computation not found";
		}

		try {
			Scanner s = new Scanner(c.getInput());
			prime = s.nextLong();
			s.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("PrimeComputation: Computation input invalid, expected \"long\"");
		}



		long factor;
		for(Job j : c.jobs) {
			UUID dataID = j.outputDataID;
			
			if(!dataID.equals(Device.NULL_UUID)) {
				Data d = Ebean.find(Data.class, dataID);
				
				if(!(d == null)) {
					Scanner scan = new Scanner(d.getContentAsString());
					
					try {
						factor = scan.nextLong();
						if(factor != 0) {
							return "Found factor for " + Long.toString(prime) + ": " + Long.toString(factor) + ".";
						}
					} catch(Exception e) {
						MyLogger.warn("Job data format invalid: " + d.data);
						e.printStackTrace();
					} finally {
						scan.close();
					}
				} else {
					MyLogger.warn("PrimeComputation: Output data for job does not exist (not in database).");
				}
			} else {
				MyLogger.warn("PrimeComputation: Output data for job does not exist (NULL_UUID).");
			}
		}
		return "No factor found for " + Long.toString(prime) + ".";
	}
}
