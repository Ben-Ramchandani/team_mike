package sitehelper;

import java.util.HashMap;
import java.util.Map;

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

	public void notify(String s) {
		this.notify();
		out.write(s);
	}

}
