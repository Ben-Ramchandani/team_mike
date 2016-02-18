package controllers;

import java.util.Random;

import play.libs.F.Callback0;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import sitehelper.ImageFactory;
import sitehelper.ImageWriter;
import views.html.*;

public class Display extends Controller{

	String computationID;
	
	public Result index(String computationID) {
		this.computationID = computationID;
		return ok(display.render());
	}

	public Result script() {
		return ok(views.js.display.render(computationID));
	}

	public WebSocket<String> socket(String computationID) {
		System.out.printf("Entered socket with computationId: %s\n",computationID);
		return new WebSocket<String>(){

		
			@Override
			public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
				System.out.printf("Entered onReady with computationId: %s\n",computationID);
				Thread t = new Thread(new ImageWriter(out));
				t.start();
				
				in.onClose(new Callback0(){
					public void invoke() {
						ImageFactory.remove(computationID);
					}
				});
			}
		};
	}
			
		
}