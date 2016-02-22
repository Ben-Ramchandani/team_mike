package twork;

import java.util.HashMap;

import models.Device;

import com.avaje.ebean.Ebean;

public class Devices {

	private Devices() {
		devices = new HashMap<String, Device>();
	}

	private static Devices instance = null;

	private HashMap<String, Device> devices;

	public static Devices getInstance() {
		if (instance == null) {
			instance = new Devices();
		}
		return instance;
	}
	
	public int getNumberOfActiveDevices() {
		return devices.size();
	}

	public Device getDevice(String sessionID) {

		if(sessionID == null) {
			MyLogger.warn("Devices.getDevice: sessionID is null, setting to \"1\".");
			sessionID = "1";
		}

		Device d = devices.get(sessionID);

		//If the device isn't loaded
		if(d == null) {
			//Look in the database
			d = Ebean.find(Device.class, Long.parseLong(sessionID));
			if(d != null) {
				d.onReload();
				devices.put(sessionID, d);
			}
		}

		//Otherwise make a new device
		if (d == null) {
			d = new Device(sessionID);
			devices.put(sessionID, d);
		}

		return d;
	}
}