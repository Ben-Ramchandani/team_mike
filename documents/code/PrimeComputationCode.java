package team_mike.server;

import java.io.InputStream;
import java.util.Scanner;

import org.json.*;
import org.omg.CORBA_2_3.portable.OutputStream;

public class PrimeComputationCode implements ComputationCode {
	private long prime;
	private long startNumber;
	private long finishNumber;
	private boolean foundFactor = false;
	private long factor = 0;

	
	//Expect input of the form
	//prime lowerBound upperBound
	public String run(InputStream input, OutputStream output) {
		
		//Parse our input data
		try {
			Scanner s = new Scanner(input);
			prime = s.nextLong();
			startNumber = s.nextLong();
			finishNumber = s.nextLong();
		} catch(Exception e) {
			e.printStackTrace();
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
			output.write(prime.toString());
		} catch(JSONException e) {
			throw new RuntimeException("PrimeComputationCode failed to package result");
		}
		return resultData.toString();
	}
}
