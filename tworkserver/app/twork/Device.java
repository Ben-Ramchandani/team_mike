package twork;

public class Device {

	public static final int NULL_JOB = 0;
	public long sessionID;
	
	public int batteryLife;
	public boolean onCharge;
	public String deviceID;

	public int jobsDone;
	public int jobsFailed;
	
	public long currentJob = NULL_JOB;
	public boolean onWiFi;
	
	public Device(long sessionID) {
		this.sessionID = sessionID;
	}
	
	public void registerJob(long jobID) {
		currentJob = jobID;
	}
	
	public void setBatteryLife(int batteryLife) {
		this.batteryLife = batteryLife;
	}
	
	
	public void setOnCharge(boolean onCharge) {
		this.onCharge = onCharge;
	}

	public long getSessionID() {
		return sessionID;
	}
}
