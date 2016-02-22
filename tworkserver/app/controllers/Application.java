package controllers;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.Computation;
import models.CustomerComputation;
import models.Data;
import models.Device;
import models.Job;
import play.mvc.Controller;
import play.mvc.Result;
import twork.ComputationManager;
import twork.Devices;
import twork.FunctionManager;
import twork.JobScheduler;
import twork.MyLogger;

import com.avaje.ebean.Ebean;

public class Application extends Controller {


	/*
	 * Available POST/GET
	 */
	public Result available() {

		/*
		 * gives the phone a UUID 
		 * starts a session
		 */

		//We could change this to give the device the full UUID and have them send it to us each time,
		//rather than only storing it server side.

		Device d;

		if (session("sessionID") == null) {
			//TODO: Should get the actual ID from the JSON
			long phoneID = UUID.randomUUID().getMostSignificantBits();
			session("sessionID", Long.toString(phoneID));
			MyLogger.log("Creating new session");
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

		return ok();
	}


	/*
	 * getJob() (GET)
	 */
	public Result job() {

		if (session("sessionID") == null) {
			return unauthorized("No session.");
		}

		Device d = Devices.getInstance().getDevice(session("sessionID"));


		//We could just cancel the current Job instead...
		if (!d.currentJob.equals(Device.NULL_UUID))  {
			return forbidden("Already have incomplete job.");
		}

		Job j = JobScheduler.getInstance().getJob(d);
		if (j == null) {
			return status(204, "NO JOB");
		}


		d.registerJob(j.jobID);
		String s = j.export();
		MyLogger.log("Handing out job with ID: " + j.jobID);
		return ok(s);
	}



	/*
	 * Data (GET)
	 */
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





	/*
	 * submit result (POST)
	 */
	public Result result(Long jobID) {

		if (session("sessionID") == null) 
			return unauthorized("No session.");

		Device d = Devices.getInstance().getDevice(session("sessionID"));

		if (d.currentJob.getLeastSignificantBits() != jobID) 
			return forbidden("Submission for incorrect job");

		byte[] r;
		String result = request().body().asText();

		if(result == null) {
			r = request().body().asRaw().asBytes();
			if(r == null) {
				return badRequest("No data found in result request");//TODO: this should fail the job.
			}
		} else {
			r = result.getBytes(StandardCharsets.UTF_8);
		}


		MyLogger.log("Recieved completed job, ID: " + d.currentJob);

		//Just pass the data straight to the Job scheduler.
		JobScheduler.getInstance().submitJob(d, r);

		//Notify device
		d.jobComplete();

		return ok();

	}


	/*
	 * Get Android code as .dex (GET)
	 */
	public Result getDexCode(String functionName) {

		byte[] b = FunctionManager.getInstance().getCodeDexDefinition(functionName);
		if(b == null) {
			MyLogger.log("Application.getDexCode, no file matching class name.");
			return notFound("Class not found (404 - Not Found).");
		}

		MyLogger.log("Sending .dex code file with name: " + functionName);
		return ok(b);
	}


	/*
	 * New: get a list of computations
	 */
	public Result getFunctions() {
		JSONObject result = new JSONObject();
		
		try {
			
			JSONObject p = new JSONObject();
			p.put("id", "PrimeComputationCode");
			p.put("name", "Prime checking");
			p.put("description", "Work out if a given number is prime.");
			
			//TODO: change this to be correct
			JSONObject i = new JSONObject();
			i.put("id", "EdgeDetect");
			i.put("name", "Image manipulation");
			i.put("description", "Do something to images.");
			
			JSONArray a = new JSONArray();
			a.put(p);
			a.put(i);
			result.put("computations", a);

		} catch (JSONException e) {
			e.printStackTrace();
			return internalServerError("Failed to prepare JSON for sending (500 - Internal Server Error).");
		}
		return ok(result.toString());
	}



	/*
	 * Not implemented
	 */
	public Result subscribe(String funId) { 
		/*
		 * feature to be added
		 * need to implement log-in features for dima's app
		 */
		return ok();
	}

	public Result function(String functionID) {
		return notFound();
	}


	/*
	 * Test targets
	 */
	public Result getClassCode(String functionName) {

		byte[] b = FunctionManager.getInstance().getCodeClassDefinition(functionName);
		if(b == null) {
			MyLogger.log("Application.getCode, no file matching class name.");
			return notFound("Class not found (404 - Not Found).");
		}

		MyLogger.log("Sending .class code file with name: " + functionName);
		return ok(b);
	}

	public Result addComputation(String custName, long prime) {
		ComputationManager cm = ComputationManager.getInstance();
		CustomerComputation custComputation = new CustomerComputation(custName, "Prime(4) example for full test", "", "PrimeComputation", Long.toString(prime));
		cm.runCustomerComputation(custComputation);
		return ok();
	}

	public Result clear_db() {
		//Check request is from localHost
		if(!(request().remoteAddress().equals("127.0.0.1") || request().remoteAddress().equals("0:0:0:0:0:0:0:1"))) {
			return forbidden("Your address is " + request().remoteAddress());
		}

		MyLogger.log("Clearing database");
		Ebean.delete(Ebean.find(Computation.class).findList());
		Ebean.delete(Ebean.find(CustomerComputation.class).findList());
		Ebean.delete(Ebean.find(Job.class).findList());
		ComputationManager.getInstance().rebuild_TEST();
		JobScheduler.getInstance().rebuild_TEST();
		return ok();
	}
}
