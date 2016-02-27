package team_mike.server;

import org.json.*;

public class PrimeJob implements Job {
	
	private long startNumber;
	private long finishNumber;
	private Computation parent;
	private long resultFactor;
	private long prime;
	public boolean hasResult = false;

	public PrimeJob(long start, long finish, long p, Computation c) {
		startNumber = start;
		finishNumber = finish;
		prime = p;
		parent = c;
	}

	public long getReturnData() {
		//Will be zero if there was no factor in the range
		return resultFactor;
	}


	public long getJobID() {
		return startNumber;
	}

	public long getComputationID() {
		return parent.getComputationID();
	}

	public Computation getParentComputation() {
		return parent;
	}

	public String getJobData() {
		try {
			JSONObject j = new JSONObject();
			j.put("prime", prime);
			j.put("startNumber", startNumber);
			j.put("finishNumber", finishNumber);
			return j.toString();
		} catch(JSONException je) {
			throw new RuntimeException("Prime:getJobData JSONException");
		}
	}

	public void addReturnData(String data) throws FormatInvalidException {
		try {
			JSONObject j = new JSONObject(data);
			boolean foundFactor = j.getBoolean("foundFactor");
			if(foundFactor) {
				resultFactor = j.getLong("factor");
			} else {
				resultFactor = 0;
			}
			hasResult = true;
		} catch(JSONException je) {
			je.printStackTrace();
			throw new FormatInvalidException();
		}
	}

	@Override
	public String getJobName() {
		return Long.toString(startNumber);
	}

	@Override
	public String getDescTitle() {
		return "Prime checker";
	}

	@Override
	public String getDescString() {
		return "";
	}

	@Override
	public boolean hasExtraJobData() {
		return false;
	}

}
