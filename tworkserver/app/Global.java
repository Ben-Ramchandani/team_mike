import java.io.File;

import models.Computation;
import models.CustomerComputation;
import models.Data;
import models.Device;
import models.Job;
import play.Application;
import play.GlobalSettings;
import play.Play;
import twork.ComputationManager;
import twork.FunctionManager;
import twork.JobScheduler;
import twork.MyLogger;

import com.avaje.ebean.Ebean;
public class Global extends GlobalSettings {

	
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files != null) {
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	}
	
	@Override
	public void onStart(Application app) {
		if(Play.isDev()) {
			MyLogger.log("Start up code running");
			MyLogger.log("Clearing database");
			Ebean.delete(Ebean.find(Computation.class).findList());
			Ebean.delete(Ebean.find(CustomerComputation.class).findList());
			Ebean.delete(Ebean.find(Job.class).findList());
			Ebean.delete(Ebean.find(Data.class).findList());
			Ebean.delete(Ebean.find(Device.class).findList());
			MyLogger.log("Deleting data files");
			
			deleteFolder(new File("data/"));

			MyLogger.log("Initializing Managers and Scheduler");
			ComputationManager.getInstance();
			JobScheduler.getInstance();
			FunctionManager.getInstance();
			MyLogger.log("Start up code finished");
		}
	}
}