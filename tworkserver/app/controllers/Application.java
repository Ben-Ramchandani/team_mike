package controllers;

import java.io.IOException;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import models.Data;
import models.Job;
import play.*;
import play.mvc.*;
import play.mvc.BodyParser.Json;
import play.mvc.Http.RequestBody;
import twork.*;
import views.html.*;

public class Application extends Controller {

	public Result index() {

		String remote = request().remoteAddress();
		return ok(remote);
	}

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
			d = Devices.getDevice(Long.parseLong(session("sessionID")));
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
		d.dataID = jobID;
		d.type = Data.TYPE_IMMEDIATE;
		d.data = "test data";

		Ebean.save(d);
	}

	public Result function(String functionID) {
		return ok();
	}

	public Result data(Long jobID) {
		//will consider this the data id so far.

		//need to ensure sane limit on the data transfer so we don't fill android's ram

		//From jobs get data id.

		if (session("sessionID") == null) 
			return unauthorized();
		Device d = Devices.getInstance().getDevice(session("sessionID"));

		if (d.currentJob != jobID) 
			return unauthorized();

		//Maybe actually the data id should just be the job id
		//Maybe actually we don't even need the data after all
		//Maybe we just need the job with the associated input?

		Long dataID = jobID; //cheat so I can test;
		Data myData = Ebean.find(Data.class,dataID);

		return ok(myData.getContent());
	}

	
	
	public Result job() {
		if (session("sessionID") == null) 
			return unauthorized();

		Device d = Devices.getInstance().getDevice(session("sessionID"));
		Job j = JobScheduler.getInstance().getJob(d);
		
		if (j == null) 
			return ok();
		if (d.currentJob != d.NULL_JOB) 
			return forbidden();

		j.retries++;
		Ebean.update(j);

		d.registerJob(j.jobID);
		String s = j.export();
		d.startCounter();

		return ok(s);
	}

	//make this request id too;
	public Result result(Long jobID) {
		if (session("sessionID") == null) 
			return unauthorized();

		Device d = Devices.getInstance().getDevice(session("sessionID"));

		if (d.currentJob != jobID) 
			return unauthorized();
		
		//Just pass it straight to the Job scheduler, we could have a job given to multiple phones, or verification to run.
		JobScheduler.getInstance().submitJob(d, request().body().asRaw().asBytes());
		
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
