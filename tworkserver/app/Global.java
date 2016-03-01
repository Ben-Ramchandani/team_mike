import play.Application;
import play.GlobalSettings;
import play.Play;
import twork.ComputationManager;
import twork.MyLogger;
public class Global extends GlobalSettings {
	
	@Override
	public void onStart(Application app) {
		if(Play.isDev()) {
			MyLogger.log("Start up code running");
			ComputationManager.getInstance().reset();
			MyLogger.log("Start up code finished");
		}
	}
}