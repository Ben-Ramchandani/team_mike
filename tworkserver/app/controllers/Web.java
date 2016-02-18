package controllers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import java.util.UUID;

import com.avaje.ebean.Ebean;

import models.Computation;
import models.CustomerComputation;
import models.Data;
import play.api.libs.concurrent.Promise;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.Results;
import twork.ComputationManager;
import twork.ComputationNotifier;
import twork.MyLogger;

import akka.actor.*;
import play.libs.F.*;
import play.mvc.WebSocket;

public class Web extends Controller{

	public Result index() {
		List<String> l = (List) Arrays.asList("Prime Computation","Image Processing");
		return ok(views.html.main.render("test", l, new play.twirl.api.Html("something")));
	}


	public Result mapFile() {
		RequestBody body = request().body();
		
		play.mvc.Http.MultipartFormData fileBody = body.asMultipartFormData();
		
		String function = "EdgeDetect";
		
		UUID computationID = UUID.randomUUID();

		List<FilePart> files = fileBody.getFiles();
		
		StringBuilder sb = new StringBuilder();

		for (FilePart filePart : files) {
			
			if (filePart != null) {
				String filename = filePart.getFilename();
				String contentType = filePart.getContentType();
				File file = filePart.getFile();
				
				UUID dataID = UUID.randomUUID();
				sb.append(dataID).append("\n");
				Data.store(file, dataID, computationID);
				MyLogger.log("File uploaded at " + dataID);
			}
			else {
				flash("error", "Missing file");
				return ok();
			}
		}
		
		UUID dataID = UUID.randomUUID();
		
		try {
			//TODO Data might be file!!
			Data.store(sb.toString(),dataID,computationID);
		} catch (IOException e) {
			 	flash("error", "Cannot store files");
		}

		String input = Ebean.find(Data.class,dataID).getContent();
		
		CustomerComputation computation = new CustomerComputation(request().remoteAddress(),"Image Processing Test","test",function,input);
		
		ComputationManager.getInstance().runCustomerComputation(computation);
		
		return redirect("/rt/" + computation.computationID.toString());
		
	}


	public Result primeTest(Long input) {
		
		CustomerComputation custComputation = new CustomerComputation(request().remoteAddress(), "Prime Computation Test", "Prime Computation Test", "PrimeComputation", input.toString());
		
		ComputationManager.getInstance().runCustomerComputation(custComputation);
	
		return ok(ComputationNotifier.getInstance().track(custComputation));
	}
	
		
}
