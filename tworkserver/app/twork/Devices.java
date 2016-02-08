package twork;

import java.util.HashMap;
import java.util.UUID;

public class Devices {
	
	private Devices() {	}
	
	private static Devices instance = null;
	
	private HashMap<String, Device> devices = new HashMap<String, Device>();
	
	public static Devices getInstance() {
		if (instance == null) {
			instance = new Devices();
		}
		return instance;
	}
	
	public String generateID() {
		return UUID.randomUUID().toString();
	}
	
	public Device getDevice(String sessionID) {
		
		if(sessionID == null) {
			sessionID = generateID();
		}
		
		Device d = devices.get(sessionID);
		
		if (d == null) {
			d = new Device(sessionID);
			devices.put(sessionID, d);
		}
		
		return d;
	}
}