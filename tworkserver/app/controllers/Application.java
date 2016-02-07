package controllers;

import java.io.File;
import java.util.UUID;

import models.Data;
import models.Job;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import twork.Device;
import twork.Devices;
import twork.JobScheduler;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	
	
	
	public Result available() {
		/*
		 * gives the phone a UUID 
		 * starts a session
		 */

		Device d; 

		if (session("sessionID") == null) {
			d = new Device(Devices.getInstance().generateID());
			session("sessionID",Long.toString(d.getSessionID())); 
		}

		else {
			d = Devices.getInstance().getDevice(Long.parseLong(session("sessionID")));
			RequestBody body = request().body();
			JsonNode jn = body.asJson();
			d.deviceID = jn.get("phone-id").asText("");
			d.batteryLife = jn.get("battery-life").asInt();
			d.onCharge = jn.get("on-charge").asBoolean();
			d.onWiFi = jn.get("on-wifi").asBoolean();
		}	
		return ok(Long.toString(d.sessionID));
	}

	public Result subscribe(Long jobID) { 
		/*
		 * feature to be added
		 * need to implement log-in features for dima's app
		 */
		return ok();
	}

	public void addTestData(Long jobID) {
		Data d = new Data();
		d.dataID = UUID.randomUUID();
		d.type = Data.TYPE_IMMEDIATE;
		d.data = "test data";

		Ebean.save(d);
	}

	public Result function(String functionID) {
		return ok();
	}

	public Result data(Long longJobID) {
		//will consider this the data id so far.

		//need to ensure sane limit on the data transfer so we don't fill android's ram

		//From jobs get data id.
		
		

		if (session("sessionID") == null) 
			return unauthorized();
		Device d = Devices.getInstance().getDevice(session("sessionID"));
		
		//The device class stores the full UUID, we just check the lower bits are right.
		UUID jobID = d.currentJob;
		
		if (jobID.getLeastSignificantBits() != longJobID) 
			return unauthorized();

		//Maybe actually the data id should just be the job id
		//Maybe actually we don't even need the data after all
		//Maybe we just need the job with the associated input?
		UUID dataID = jobID; //cheat so I can test;
		Data myData = Ebean.find(Data.class, dataID);

		return ok(myData.getContent());
	}

	
	
	public Result job() {
		if (session("sessionID") == null) 
			return unauthorized();

		Device d = Devices.getInstance().getDevice(session("sessionID"));
		Job j = JobScheduler.getInstance().getJob(d);
		
		if (j == null) 
			return status(555, "NO JOB");
		if (d.currentJob != Device.NULL_UUID) 
			return forbidden();

		//Done in the Scheduler
		//d.registerJob(j.jobID);
		d.startCounter();
		String s = j.export();

		return ok(s);
	}

	//make this request id too;
	public Result result(Long jobID) {
		if (session("sessionID") == null) 
			return unauthorized();

		Device d = Devices.getInstance().getDevice(session("sessionID"));

		if (d.currentJob.getLeastSignificantBits() != jobID) 
			return unauthorized();
		
		//Just pass it straight to the Job scheduler, we could have a job given to multiple phones, or verification to run.
		JobScheduler.getInstance().submitJob(d, request().body().asText());
		
		//Old code for reference, can be deleted if you want
		/*
		String s = request().body().asText();
		Job j = Ebean.find(Job.class,d.currentJob);
		try {
			if (Data.store(s,j.outputDataID,j.computationID) == false) 
				return unauthorized();
		}
		catch (IOException e) {
			return this.internalServerError();
		}
		//TODO notify the computation that it has one less job to do.
		d.jobsDone++;
		*/
		
		return ok();
	}
}
