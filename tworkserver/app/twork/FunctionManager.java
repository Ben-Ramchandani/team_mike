package twork;
import computations.BasicComputationGenerator;
import computations.ComputationCode;
import computations.PrimeComputation;
import computations.PrimeComputationCode;

public class FunctionManager {
	//Placeholder
	public static BasicComputationGenerator getBasicComputationGenerator(String name) {
		return new PrimeComputation();
	}
}
