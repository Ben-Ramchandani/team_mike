package twork;
import computations.BasicComputationGenerator;
import computations.PrimeComputation;

public class FunctionManager {
	
	private static FunctionManager instance;
	
	private FunctionManager() {}
	
	public static FunctionManager getInstance() {
		if(instance == null) {
			instance = new FunctionManager();
		}
		return instance;
	}
	
	//Obviously this should be changed for the server
	public static final String computationCodePath = "/media/ben/hdd_free/programming/team_mike/computations/";
	
	//Placeholder
	public BasicComputationGenerator getBasicComputationGenerator(String name) {
		return new PrimeComputation();
	}
}
