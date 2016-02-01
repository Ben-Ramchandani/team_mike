package controllers;

import com.fasterxml.jackson.databind.JsonNode;

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
    		
    		//need to make these secure;
    		d.deviceID = jn.get("phone-id").asText("");
    		d.batteryLife = jn.get("battery-life").asInt();
    		d.onCharge = jn.get("on-charge").asBoolean();
    		d.onWiFi = jn.get("on-wifi").asBoolean();
    	}	
    	return ok();
    }
    
    public Result subscribe(Long id) { 
    	/*
    	 * feature to be added
    	 * need to implement log in features for dima's app
    	 */
    	return ok();
    }
    
    public Result job() {
    	/*
    	 * request a job. (probably should be POST)
    	 * check priority based on sessionID.
    	 * should provide 
    	*/
    	if (session("sessionID") == null) 
        	return unauthorized();
    	
    	Device d = Devices.getDevice(session("sessionID")) {
    	Job j = JobScheduler.getJob();
    	}
    	
    }
    
    public Result result(Long id) {
    	return ok();
    }

}
