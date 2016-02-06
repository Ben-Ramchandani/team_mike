package twork;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.avaje.ebean.Ebean;

import models.Job;

public class Device {

	public static final int NULL_JOB = 0;
	public static final int WAITING_TIME = 50;
	public long sessionID;
	
	public int batteryLife;
	public boolean onCharge;
	public String deviceID;

	public int jobsDone;
	public int jobsFailed;
	
	public long currentJob = NULL_JOB;
	public boolean onWiFi;
	
	
	public Timer t = new Timer();
	
	public Device(long sessionID) {
		this.sessionID = sessionID;
	}
	
	public synchronized void registerJob(UUID jobID) {
		currentJob = jobID.getLeastSignificantBits();
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
				trigger.currentJob = NULL_JOB;
			}
		}
		
	}
	
}
