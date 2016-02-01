package team_mike.server;

import java.io.InputStream;

import org.json.*;

public class PrimeComputationCode implements ComputationCode {
	private long prime;
	private long startNumber;
	private long finishNumber;
	private boolean foundFactor = false;
	private long factor = 0;

	public String run(String jobData, InputStream ignore, InputStream ignore0) {
		
		//Parse our input data
		try {
			JSONObject jData = new JSONObject(jobData);
			prime = jData.getLong("prime");
			startNumber = jData.getLong("startNumber");
			finishNumber = jData.getLong("finishNumber");
		} catch(JSONException e) {
			throw new RuntimeException("PrimeComputationCode failed to parse arguments.");
		}
		

		//The checking for a factor
		//Super inefficient I know
		for(long i = startNumber; i<finishNumber; i++) {
			if(prime % i == 0) {
				factor = i;
				foundFactor = true;
				break;
			}
		}


		//Package up our result
		JSONObject resultData;
		try {
			resultData = new JSONObject();
			resultData.put("job", "prime");
			resultData.put("foundFactor", foundFactor);
			resultData.put("factor", factor);
		} catch(JSONException e) {
			throw new RuntimeException("PrimeComputationCode failed to package result");
		}
		return resultData.toString();
	}
}
