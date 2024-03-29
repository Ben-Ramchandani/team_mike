package controllers;

import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.UUID;

import models.Computation;
import models.CustomerComputation;
import models.Data;
import models.Device;
import models.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Play;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import sitehelper.Metadata;
import twork.ComputationManager;
import twork.Devices;
import twork.FunctionManager;
import twork.JobScheduler;
import twork.MyLogger;

import com.avaje.ebean.Ebean;

public class Application extends Controller {


	/*
	 * Available (POST/GET)
	 */
	public Result available() {

		/*
		 * gives the phone a UUID 
		 * starts a session
		 */


		String phoneID;
		String body = request().body().asText();
		//Attempt to parse the phone ID from the body
		if(body == null) {
			MyLogger.warn("No body found in /available request");
			if (session("sessionID") == null) {
				MyLogger.warn("No phoneID found, will generate one at random.");
				phoneID = UUID.randomUUID().toString();
			} else {
				phoneID = session("sessionID");
			}
		} else {
			try {
				JSONObject json = new JSONObject(body);
				phoneID = json.getString("phone-id");
			} catch (Exception e) {
				MyLogger.log("Failed to parse JSON from /available request.");
				e.printStackTrace();
				return badRequest("Failed to parse JSON (400 - Bad Request).");
			}
		}

		if(session("sessionID") != null) {
			//Check session and parsed ID agree
			if( !( phoneID.equals(session("sessionID")) ) ) {
				MyLogger.critical("Session ID disagrees with claimed phone ID.");
				return badRequest("Session ID disagrees with phone ID (400 - Bad Request).");
			}
		} else {
			session("sessionID", phoneID);
			MyLogger.log("Creating new session with ID: " + session("sessionID"));
		}

		Device d;


		d = Devices.getInstance().getDevice(session("sessionID"));
		Metadata.increaseNumberDevices();
		//We'll worry about this later - nothing on the server depends on this.
		/*
		RequestBody body = request().body();


		try {
			JsonNode jn = body.asJson();
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
		Metadata.decreaseNumberDevices();
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
	@BodyParser.Of(value=BodyParser.Raw.class, maxLength=100*1024*1024)
	public Result result(Long jobID) {

		if (session("sessionID") == null) 
			return unauthorized("No session.");

		Device d = Devices.getInstance().getDevice(session("sessionID"));

		if (d.currentJob.getLeastSignificantBits() != jobID) 
			return forbidden("Submission for incorrect job");

		byte[] r;
		
		MyLogger.log("Received result. Length: " + request().getHeader(CONTENT_LENGTH) + ", type: " + request().getHeader(CONTENT_TYPE));
		String result = request().body().asText();

		if(result == null) {

			r = request().body().asRaw().asBytes(100*1024*1024);
			if(r == null) {
				MyLogger.alwaysLog("No data in result request.");
				//This could be used to instead fail the job
				return badRequest("No data found in result request");
			}
		} else {
			r = result.getBytes(StandardCharsets.UTF_8);
		}


		MyLogger.log("Recieved completed job, ID: " + d.currentJob);

		//Just pass the data straight to the Job scheduler.
		
		JobScheduler.getInstance().submitJob(d, r);

		//Notify device
		d.jobComplete();
		Metadata.increaseNumberDevices();
		
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
			p.put("topics", "Cryptography, Examples");

			JSONObject i = new JSONObject();
			i.put("id", "EdgeDetect");
			i.put("name", "Emboss effect");
			i.put("description", "Emphasises areas of different colour.");
			i.put("topics", "Image processing, Examples");
			
			JSONObject j = new JSONObject();
			j.put("id", "GrayscaleConvertCode");
			j.put("name", "Grayscale");
			j.put("description", "Converts colour images to grayscale.");
			j.put("topics", "Image processing, Examples");
			
			JSONObject k = new JSONObject();
			k.put("id", "SepiaConvertCode");
			k.put("name", "Sepia");
			k.put("description", "Applies a Sepia filter to images.");
			k.put("topics", "Image processing, Examples");

			JSONArray a = new JSONArray();
			a.put(p);
			a.put(i);
			a.put(j);
			a.put(k);
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
		return ok();
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
	
	public Result adminInfo() {
		int numComputations = Ebean.find(Computation.class).findRowCount();
		int totalComputations = Ebean.find(CustomerComputation.class).findRowCount();
		int numJobs = Ebean.find(Job.class).findRowCount();
		int numDevices = Devices.getInstance().getNumberOfActiveDevices();
		long memory = Runtime.getRuntime().totalMemory();
		
		StringBuilder sb = new StringBuilder();
    	Formatter formatter = new Formatter(sb);
    	
    	formatter.format("<html><head><title>Twork - Server information</title><meta http-equiv=\"refresh\" content=\"10\"></head><body><ul>" +
    			"<h1>Twork server information</h1>" +
    			"<li>Total number of computations: %d</li>" +
    			"<li>Number of running computations: %d</li>" +
    			"<li>Number of jobs: %d</li>" +
    			"<li>Number of connected devices: %d</li>" +
    			"<li>Memory usage: %d KB</li>" +
    			"</ul></body></html>", totalComputations, numComputations, numJobs, numDevices, memory/1024);
    	
    	String result = formatter.out().toString();
    	formatter.close();
		return ok(result).as("text/html");
	}

	public Result addComputation(String custName, long prime) {
		ComputationManager cm = ComputationManager.getInstance();
		CustomerComputation custComputation = new CustomerComputation(custName, "Prime(4) example for full test", "", "PrimeComputation", Long.toString(prime));
		cm.runCustomerComputation(custComputation);
		return ok();
	}

	public Result reset(String key) {
		if(!key.equals("x")) {
			return forbidden();
		}
		MyLogger.log("System reset started");
		ComputationManager.getInstance().reset();
		System.gc();
		MyLogger.log("System reset complete");
		return ok();
	}

}
