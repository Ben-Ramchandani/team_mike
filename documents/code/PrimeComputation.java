package team_mike.server;

//A simple computation that tries to find out if a number is prime.

public class PrimeComputation implements Computation {

	private long prime = 22801763489L;
	private String name = "prime-0.0.1";

	//Is the computation finished?
	private boolean exausted = false;

	//Have we run out of jobs?
	private boolean complete = false;

	//Our example factor
	private long factor;

	//The current number we're up to in our search
	private long currentNum;

	private final static long numPerJob = 10000;

	private long stopNum;



	public PrimeComputation(long p) {
		prime = p;
		currentNum = 2;
		stopNum = prime - 1;
	}

	public long getComputationID() {
		//Placeholder, there should be a class in the server that gives these out.
		return 1L;
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean isExausted() {
		return exausted;
	}

	public synchronized Job getJob() {
		if(exausted || complete) {
			return null;
		}
		
		long uptoNum;
		if(currentNum + numPerJob >= stopNum) {
			uptoNum = stopNum;
		} else {
			uptoNum = numPerJob + currentNum;
		}

		PrimeJob j = new PrimeJob(currentNum, uptoNum, prime, this);
		currentNum = uptoNum;

		if(currentNum >= stopNum) {
			exausted = true;
		}
		return j;
	}

	public String getName() {
		return name;
	}

	public boolean hasExtraComputationData() {
		return false;
	}

	public synchronized void submitJob(Job toSubmit) throws FormatInvalidException, JobInvalidException {
		if(complete) {
			throw new RuntimeException("PrimeComputation job submitted after computation has completed");
		}

		//Check we have the right type of job.
		PrimeJob j;
		try {
			j = (PrimeJob) toSubmit;
		} catch(ClassCastException e) {
			System.err.println("PrimeComputation job of wrong type");
			e.printStackTrace();
			throw new FormatInvalidException();
		}
		//Check the job has been run
		if(!j.hasResult) {
			System.err.println("Job has not completed");
			throw new FormatInvalidException();
		}

		long fact = j.getReturnData();
		if(fact != 0) {//factor found
			if(prime % fact == 0) {//and is correct
				complete(fact);
			} else {
				throw new JobInvalidException();
			}
		}
	}

	private synchronized void complete(long f) {
		complete = true;
		exausted = true;
		factor = f;
	}

	public synchronized String getResult() {
		if(complete) {
			if(factor == 0) {
				return String.valueOf(prime) + " is prime";
			} else {
				return String.valueOf(prime) + " is not prime and has factor " + String.valueOf(factor);
			}
		} else {
			throw new RuntimeException("PrimeComputation getResult: computation not complete yet.");
		}
	}

	public boolean isJobValidationEnabled() {
		return true;
	}

	public void reset() {
		exausted = complete = false;
		currentNum = 2;
	}
}
