package twork;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Device {

	public static final UUID NULL_UUID = new UUID( 0L , 0L );
	//5 minutes (in milliseconds).
	public static final int WAITING_TIME = 300000;
	public String sessionID;
	
	public int batteryLife;
	public boolean onCharge;
	public String deviceID;

	public int jobsDone;
	public int jobsFailed;
	
	public UUID currentJob;
	public boolean onWiFi;
	
	
	//A better performing solution would be to have a single timer for all devices.
	public Timer t;
	
	public Device(String sessionID) {
		this.sessionID = sessionID;
		currentJob = NULL_UUID;
	}
	
	public synchronized void registerJob(UUID jobID) {
		if(currentJob.equals(NULL_UUID)) {
			currentJob = jobID;
			startTimer();
		} else if(currentJob.equals(jobID)) {
			return;
		} else {
			MyLogger.warn("Device.registerJob: Already have job, register anyway. The timer will be terminated without running.");
			cancelTimer();
			currentJob = jobID;
			startTimer();
		}
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
	
	public synchronized void jobComplete() {
		currentJob = NULL_UUID;
		cancelTimer();
		jobsDone++;
	}
	
	private void cancelTimer() {
		if(t != null) {
			t.cancel();
		}
	}
	
	private void startTimer() {
		//Run as daemon
		t = new Timer(true);
		t.schedule(new TimeoutJob(this), WAITING_TIME);
	}
	
	public static class TimeoutJob extends TimerTask {
		
		public Device trigger;
		
		public TimeoutJob(Device d) {
			trigger = d;
		}
		
		@Override
		public void run() {
			UUID jobID = trigger.currentJob;
			synchronized(trigger) {
				//The job timed out.
				MyLogger.log("Device: Job has been timed out");
				trigger.jobsFailed++;
				trigger.currentJob = NULL_UUID;
			}
			JobScheduler.getInstance().timeoutJob(jobID);
		}
		
	}
	
}
