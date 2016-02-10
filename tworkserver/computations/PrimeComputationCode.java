import java.io.InputStream;
import java.util.Scanner;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PrimeComputationCode implements ComputationCode {
	private long prime;
	private long startNumber;
	private long finishNumber;
	private long factor = 0;

	
	//Expect input of the form
	//prime lowerBound upperBound
	@Override
	public void run(InputStream input, OutputStream output) {

		//Parse our input data
		try {
			Scanner s = new Scanner(input);
			prime = s.nextLong();
			startNumber = s.nextLong();
			finishNumber = s.nextLong();
			s.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("PrimeComputationCode failed to parse arguments.");
		}
		

		//The checking for a factor
		//Super inefficient I know
		for(long i = startNumber; i<finishNumber; i++) {
			if(prime % i == 0) {
				factor = i;
				break;
			}
		}


		//Package up our result
		//0 means no factor found
		String result = Long.toString(factor);
		try {
			output.write(result.getBytes(StandardCharsets.UTF_8));
		} catch(Exception e) {
			throw new RuntimeException("PrimeComputationCode failed to package result");
		}

		return;
	}
}
