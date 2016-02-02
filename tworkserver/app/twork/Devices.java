package twork;

import java.util.HashMap;
import java.util.Random;

public class Devices {
	
	private Devices() {
		
	}
	
	private static Devices instance = null;
	
	private static HashMap<Long,Device> devices = new HashMap<Long,Device>();
	
	public static Devices getInstance() {
		if (instance == null) {
			instance = new Devices();
		}
		return instance;
	}
	
	public int generateID() {
		
		/*
		 * TODO: check for unique identifier
		 */
		return (new Random()).nextInt();
	}
	
	public static Device getDevice(long sessionID) {
		Device d = devices.get(sessionID);
		if (d == null) {
			d = new Device(sessionID);
			devices.put(sessionID, d);
		}
		return d;
	}

	public static Device getDevice(String session) {
		return getDevice(Long.parseLong(session));
	}
}