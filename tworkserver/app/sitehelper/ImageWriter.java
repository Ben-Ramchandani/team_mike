package sitehelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import models.Data;
import play.mvc.WebSocket;

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

	public void notify(byte[] s) {
		//Need to create a file here
		//this.notify();
		Data d = null; 
		//try {
			//TODO bring computation id here.
			//d = Data.store(s, UUID.randomUUID(), UUID.randomUUID());
			//System.out.println(s.length());
		//} catch (IOException e) {
			//System.out.println("Failed");
			//e.printStackTrace();
		//}
		//d.update();
		//out.write("http://localhost:9000/page/retrieve/" + d.data);
	}

}
