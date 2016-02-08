package twork;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Device {

	public static final UUID NULL_UUID = new UUID( 0L , 0L );
	public static final int WAITING_TIME = 50;
	public String sessionID;
	
	public int batteryLife;
	public boolean onCharge;
	public String deviceID;

	public int jobsDone;
	public int jobsFailed;
	
	public UUID currentJob;
	public boolean onWiFi;
	
	
	public Timer t = new Timer();
	
	public Device(String sessionID) {
		this.sessionID = sessionID;
		currentJob = NULL_UUID;
	}
	
	public synchronized void registerJob(UUID jobID) {
		currentJob = jobID;
	}
	
	public void setBatteryLife(int batteryLife) {
		this.batteryLife = batteryLife;
	}
	
	
	public void setOnCharge(boolean onCharge) {
		this.onCharge = onCharge;
	}

	public String getSessionID() {
		return sessionID;
	}
	
	
	public void startCounter() {
		t.schedule(new TimeoutTask(this), WAITING_TIME);
	}
	
	public static class TimeoutTask extends TimerTask {
		
		Device trigger;
		
		public TimeoutTask(Device d) {
			trigger = d;
		}
		
		@Override
		public void run() {
			synchronized(trigger) {
				trigger.jobsFailed++;
				JobScheduler.getInstance().timeoutJob(trigger.currentJob);
				trigger.currentJob = NULL_UUID;
			}
		}
		
	}
	
}
