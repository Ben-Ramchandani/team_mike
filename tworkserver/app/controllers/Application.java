package controllers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import models.Computation;
import models.CustomerComputation;
import models.Data;
import models.Job;
import play.mvc.Controller;
import play.mvc.Result;
import twork.Device;
import twork.Devices;
import twork.FunctionManager;
import twork.JobScheduler;
import twork.MyLogger;

import com.avaje.ebean.Ebean;

import play.mvc.Http.RequestBody;

public class Application extends Controller {

	
	
	public Result available() {
		
		
		
		
		
		/*
		 * gives the phone a UUID 
		 * starts a session
		 */
		
		//We could change this to give the device the full UUID and have them send it to us each time,
		//rather than only storing it server side.

		Device d; 

		if (session("sessionID") == null) {
			d = new Device(Devices.getInstance().generateID());
			session("sessionID", d.getSessionID()); 
		}

		
		d = Devices.getInstance().getDevice(session("sessionID"));
		//We'll worry about this later - nothing on the server depends on this.
		/*
		RequestBody body = request().body();

		
		try {
			JsonNode jn = body.asJson();
			d.deviceID = jn.get("phone-id").asText("");
			d.batteryLife = jn.get("battery-life").asInt(0);
			d.onCharge = jn.get("on-charge").asBoolean(true);
			d.onWiFi = jn.get("on-wifi").asBoolean(true);
		} catch(Exception e) {
			session().clear();
			return badRequest("Could not parse JSON.");
		}
		*/
		

		return ok(d.sessionID);
	}

	public Result subscribe(Long jobID) { 
		/*
		 * feature to be added
		 * need to implement log-in features for dima's app
		 */
		return notFound();
	}

	public void addTestData(Long jobID) {
		Data d = new Data();
		d.dataID = UUID.randomUUID();
		d.type = Data.TYPE_IMMEDIATE;
		d.data = "test data";

		Ebean.save(d);
	}

	public Result function(String functionID) {
		return notFound();
	}

	public Result data(Long longJobID) {

		if (session("sessionID") == null) 
			return unauthorized();
		Device d = Devices.getInstance().getDevice(session("sessionID"));

		//The device class stores the full UUID, we just check the lower bits are right.
		UUID jobID = d.currentJob;

		if (jobID.getLeastSignificantBits() != longJobID) 
			return unauthorized();


		Job j = Ebean.find(Job.class, jobID);
		if(j == null) {
			MyLogger.warn("Request for data: non-existent job");
			return notFound("Request for data: Server has no record of job (404 Not Found).");
			
		}
		
		//TODO: data dependence
		UUID dataID = j.inputDataID;
		if(dataID == null) {
			MyLogger.warn("Request for data: no data in job");
			return internalServerError("Request for data: No data is attached to job (500 Internal Server Error).");
		}
		
		Data myData = Ebean.find(Data.class, dataID);
		if(myData == null) {
			MyLogger.warn("Request for data: data id in job does not exist");
			return internalServerError("Request for data: Data id in job does not map to anything (500 Internal Server Error).");
		}
		
		return ok(myData.getContent());
	}



	public Result job() {
		
		if (session("sessionID") == null) {
			return unauthorized("No session.");
		}
		
		Device d = Devices.getInstance().getDevice(session("sessionID"));
		
		if (!d.currentJob.equals(Device.NULL_UUID))  {
			return forbidden("Already have incomplete job.");//TODO: cancel current job.
		}
		
		Job j = JobScheduler.getInstance().getJob(d);
		if (j == null) {
			return status(555, "NO JOB");
		}
		
		
		d.registerJob(j.jobID);
		String s = j.export();
		return ok(s);
	}

	
	public Result result(Long jobID) {
		
		if (session("sessionID") == null) 
			return unauthorized("No session.");

		Device d = Devices.getInstance().getDevice(session("sessionID"));

		if (d.currentJob.getLeastSignificantBits() != jobID) 
			return forbidden("Submission for incorrect job");
			
		
		String result = request().body().asText();
		
		if(result == null) {
			return badRequest("No data found in result request");//TODO: this should fail the job.
		}
		
		
		//Just pass the data straight to the Job scheduler.
		JobScheduler.getInstance().submitJob(d, result);
		
		//Notify device
		d.jobComplete();

		return ok();
		
	}
	
	public Result getCode(String functionName) {
		
		byte[] b = FunctionManager.getInstance().getCodeClassDefinition(functionName);
		if(b == null) {
			MyLogger.log("getCode, job requested has no code.");
			return notFound("Class not found (404 - Not Found).");
		}
		
		return ok(b);
	}
}
