package twork;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import computations.BasicComputationGenerator;
import computations.PrimeComputation;

public class FunctionManager {

	private static FunctionManager instance;
	//This needs to be changed
	public static final String computationCodePath = "computations/";
	private static List<String> computationCodeNames; 

	private FunctionManager() {
		//TODO: dynamically load these
		computationCodeNames = new ArrayList<String>();
		computationCodeNames.add("PrimeComputationCode");
		computationCodeNames.add("ComputationCode");
		//TODO:remove
		computationCodeNames.add("tworkservertest");
	}

	public static FunctionManager getInstance() {
		if(instance == null) {
			instance = new FunctionManager();
		}
		return instance;
	}




	public byte[] getCodeClassDefinition(String name) {
		//Remove ".class" from the name if it is there
		String filePrefix = name;
		if(name.length() > 6) {
			String end = name.substring(name.length() - 6, name.length());
			if(end.equals(".class")) {
				filePrefix = name.substring(0, name.length() - 6);
			}
		}

		if(!computationCodeNames.contains(filePrefix)) {
			MyLogger.log("FunctionManager.getCodeClassDefinition: Computation requested does not exist: " + name + ".");
			return null;
		}


		try {
			return Files.readAllBytes(Paths.get(computationCodePath + filePrefix + ".class"));
		} catch(IOException e) {
			MyLogger.log("FunctionManager.getCodeClassDefinition: File read failed.");
			e.printStackTrace();
			return null;
		}
	}


	
	public byte[] getCodeDexDefinition(String name) {
		//Remove ".dex" from the name if it is there
		String filePrefix = name;
		if(name.length() > 6) {
			String end = name.substring(name.length() - 4, name.length());
			if(end.equals(".dex")) {
				filePrefix = name.substring(0, name.length() - 4);
			}
		}

		if(!computationCodeNames.contains(filePrefix)) {
			MyLogger.log("FunctionManager.getCodeClassDefinition: Computation requested does not exist: " + name + ".");
			return null;
		}


		try {
			return Files.readAllBytes(Paths.get(computationCodePath + filePrefix + ".dex"));
		} catch(IOException e) {
			MyLogger.log("FunctionManager.getCodeClassDefinition: File read failed.");
			e.printStackTrace();
			return null;
		}
	}

	//Placeholder
	public BasicComputationGenerator getBasicComputationGenerator(String name) {
		return new PrimeComputation();
	}
}
