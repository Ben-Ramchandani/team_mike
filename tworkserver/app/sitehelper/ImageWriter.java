package sitehelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import models.Data;
import play.mvc.WebSocket;
import twork.MyLogger;

public class ImageWriter implements Runnable {

	WebSocket.Out<String> out;

	
	public ImageWriter(WebSocket.Out<String> out) {
		this.out = out;
	}

	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			out.write("http://www.metastatic.org/images/gnu-crypto/gnu-crypto-2-small.png");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
