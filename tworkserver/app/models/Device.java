package models;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import twork.JobScheduler;
import twork.MyLogger;

import com.avaje.ebean.Model;

@Entity
@Table(name = "all_device")
public class Device extends Model {

	@Transient
	public static final UUID NULL_UUID = new UUID( 0L , 0L );
	//5 minutes (in milliseconds).
	@Transient
	public static final int WAITING_TIME = 300000;
	
	@Id
	public long deviceID;
	
	public int jobsDone;
	public int jobsFailed;
	
	//These change too much to be worth storing
	@Transient
	private String sessionID;
	@Transient
	public UUID currentJob;
	@Transient
	public int batteryLife;
	@Transient
	public boolean onCharge;
	@Transient
	public boolean onWiFi;
	
	@Transient
	public long lastAccessTimestamp;
	
	
	//A better performing solution would be to have a single timer for all devices.
	@Transient
	public Timer t;
	
	
	public Device(String sessionID) {
		//Tests pass in "1" if they are not concerned with the workings of this class.
		if(sessionID == "1") {
			sessionID = Long.toString(UUID.randomUUID().getLeastSignificantBits());
		}
		
		
		this.sessionID = sessionID;
		deviceID = Long.parseLong(sessionID);
		currentJob = NULL_UUID;
		updateTimestamp();
		this.save();
	}
	
	private void updateTimestamp() {
		lastAccessTimestamp = (new Date()).getTime();
	}
	
	public synchronized void registerJob(UUID jobID) {
		updateTimestamp();
		
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
		this.update();
	}
	
	private void cancelTimer() {
		updateTimestamp();
		if(t != null) {
			t.cancel();
		}
	}
	
	@Override
	public void update() {
		updateTimestamp();
		super.update();
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
				trigger.update();
				trigger.currentJob = NULL_UUID;
			}
			JobScheduler.getInstance().timeoutJob(jobID);
		}
		
	}
	
}
