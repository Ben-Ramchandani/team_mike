package models;

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
	//45 seconds (in milliseconds).
	@Transient
	public static final int WAITING_TIME = 45000;
	
	@Id
	public String deviceID;
	
	public int jobsDone;
	public int jobsFailed;
	
	//These change too much to be worth storing
	@Transient
	public UUID currentJob;
	@Transient
	public int batteryLife;
	@Transient
	public boolean onCharge;
	@Transient
	public boolean onWiFi;
	
	
	
	//A better performing solution would be to have a single timer for all devices.
	@Transient
	public Timer t;
	
	
	public Device(String phoneID) {
		//Tests pass in "1" if they are not concerned with the workings of this class.
		if(phoneID == "1") {
			MyLogger.warn("phoneID is 1, generating new ID at random");
			phoneID = UUID.randomUUID().toString();
		}
		
		
		deviceID = phoneID;
		currentJob = NULL_UUID;
		this.save();
	}
	
	//When taken out of the database
	public void onReload() {
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
		return deviceID;
	}
	
	public synchronized void jobComplete() {
		currentJob = NULL_UUID;
		cancelTimer();
		jobsDone++;
		this.update();
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
				trigger.update();
				trigger.currentJob = NULL_UUID;
			}
			JobScheduler.getInstance().timeoutJob(jobID);
		}
		
	}
	
}
