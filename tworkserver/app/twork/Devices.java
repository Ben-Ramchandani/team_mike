package twork;

import java.util.Random;

public class Devices {
	private Devices() {
		
	}
	
	private static Devices instance = null;
	
	public static Devices getInstance() {
		if (instance == null) {
			instance = new Devices();
		}
		return instance;
	}
	
	public static int generateID() {
		
		/*
		 * TODO: check for unique identifier
		 */
		return (new Random()).nextInt();
	}
	
	
	
}
