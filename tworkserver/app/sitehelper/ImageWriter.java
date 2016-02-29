package sitehelper;

import java.util.UUID;

import play.mvc.WebSocket;
import twork.MyLogger;

public class ImageWriter {

	WebSocket.Out<String> out;

	
	public ImageWriter(WebSocket.Out<String> out) {
		this.out = out;
	}


	public void notify(UUID dataID) {
		//Need to create a file here
		//this.notify();
		
		if (dataID == null) {
			MyLogger.warn("Data output is missing.");
			return;
		}
		out.write("http://ec2-52-36-182-104.us-west-2.compute.amazonaws.com:9000/page/retrieve/" + dataID);
	}

}
