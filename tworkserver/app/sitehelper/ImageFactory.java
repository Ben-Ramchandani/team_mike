package sitehelper;

import java.util.HashMap;
import java.util.Map;

import play.mvc.WebSocket;

public abstract class ImageFactory {

	
	private static Map<String,ImageWriter> instances = new HashMap<String,ImageWriter>();

	public static ImageWriter newInstance(String computationID, WebSocket.Out<String> out) {
		ImageWriter i = new ImageWriter(out);
		instances.put(computationID,i);
		return i;
	}
	
	public static void notify(String computationID, String s) {
		ImageWriter imageWriter;
		if ((imageWriter = instances.get(computationID))!=null)
			imageWriter.notify(s);
	}

	public static void remove(String computationID) {
		instances.remove(computationID);
		
	}

}
