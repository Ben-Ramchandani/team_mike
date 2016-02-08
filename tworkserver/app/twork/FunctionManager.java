package twork;
import computations.BasicComputationGenerator;
import computations.PrimeComputation;

public class FunctionManager {
	
	public static final String computationCodePath = "/media/ben/hdd_free/programming/team_mike/computations/";
	
	public void addComputation(String functionName, String computationName, String input) throws Exception {
		if(!functionName.equals("PrimeComputation")) {
			throw new Exception("Function not supported");
		} else {
			ComputationManager.getInstance().addBasicComputation(new PrimeComputation(), input);
		}
	}
	
	//Placeholder
	public static BasicComputationGenerator getBasicComputationGenerator(String name) {
		return new PrimeComputation();
	}
}
