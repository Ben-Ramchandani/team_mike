package controllers;

import play.*;
import play.mvc.*;
import twork.Devices;
import views.html.*;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public Result available() {
    	/* 
    	 *give device an id
    	 *
    	 *
    	 */
    	long id = Devices.getInstance().generateID();
    	return ok("your id is " + id);
    }

    public Result requestJob() {
    	
    	
    }
}
