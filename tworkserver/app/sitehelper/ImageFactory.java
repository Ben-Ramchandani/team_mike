package sitehelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import play.mvc.WebSocket;
import twork.MyLogger;

public abstract class ImageFactory {

	
	private static Map<String,ImageWriter> instances = new HashMap<String,ImageWriter>();

	public static ImageWriter newInstance(String computationID, WebSocket.Out<String> out) {
		ImageWriter i = new ImageWriter(out);
		instances.put(computationID,i);
		return i;
	}
	
	public static void notify(String computationID, UUID dataId) {
		ImageWriter imageWriter;
		if ((imageWriter = instances.get(computationID))!=null)
		{ 	
			MyLogger.log("Notifying browser of completed image job");
			imageWriter.notify(dataId);
		}
	}

	public static void remove(String computationID) {
		instances.remove(computationID);
		
	}
	
	public static void reset() {
		instances = new HashMap<String,ImageWriter>();
	}
}
