package computations;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

import models.Computation;
import models.Data;
import models.Job;
import twork.Device;

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

		//TODO: this constructor
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
				UUID dataID = UUID.randomUUID();

				try {
					Data d = Data.store(jobInput, dataID, c.computationID);
					d.save();
				} catch (IOException e) {
					System.err.println("Data store failed with IOException");
					e.printStackTrace();
					throw new RuntimeException("generateComputation failed");
				}

				Job j = new Job(c, "Prime job", dataID, functionName);
				j.save();
				c.jobs.add(j);

				currentStart = currentEnd;
				currentEnd += numPerJob;
			}

			c.running = true;
			c.jobsLeft = c.jobs.size();
			c.input = input;
			c.update();
			Ebean.commitTransaction();
		} finally {
			Ebean.endTransaction();
		}
		// ### End transaction ###
		return c.computationID;
	}

	public String getResult(UUID computationID) {
		long prime;
		Computation c = Ebean.find(Computation.class, computationID);
		if(c == null) {
			return "Error: computation not found";
		} else if(c.jobsLeft > 0) {
			return "Error: computation not complete";
		} else if(c.failed) {
			return "Error: computation failed";
		} else if(c.completed) {
			return "Error: computation has aleady been processed";
		}

		try {
			Scanner s = new Scanner(c.input);
			prime = s.nextLong();
			s.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("PrimeComputation: Computation input invalid, expected \"long\"");
		}

		c.running = false;
		c.completed = true;
		c.update();


		long factor;
		for(Job j : c.jobs) {
			UUID dataID = j.outputDataID;
			if(!dataID.equals(Device.NULL_UUID)) {
				//TODO: dependent on the data class.
				Data d = Ebean.find(Data.class, dataID);
				if(!(d == null)) {
					Scanner scan = new Scanner(d.data);
					try {
						factor = scan.nextLong();
						if(factor != 0) {
							return "Found factor for " + Long.toString(prime) + ": " + Long.toString(factor) + ".";
						}
					} finally {
						scan.close();
					}
				}
			}
		}
		return "No factor found for " + Long.toString(prime) + ".";
	}
}
