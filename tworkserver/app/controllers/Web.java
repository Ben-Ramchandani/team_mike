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
import models.Device;
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

		UUID computationID = Device.NULL_UUID;

		List<FilePart> files = fileBody.getFiles();

		StringBuilder sb = new StringBuilder();

		UUID dataID = null;
		for (FilePart filePart : files) {

			if (filePart != null) {
				String filename = filePart.getFilename();
				String contentType = filePart.getContentType();
				File file = filePart.getFile();

				dataID = Data.store(file);
				sb.append(dataID).append("\n");
				MyLogger.log("File uploaded at " + dataID);
			}
			else {
				flash("error", "Missing file");
				return ok();
			}
		}

		if(dataID == null) {
			return badRequest();
		}

		//Why is this stored as well?
		/*
		UUID dataID = UUID.randomUUID();

		Data d = null;
		try {
			d = Data.storeString(sb.toString(),dataID,computationID);
		} catch (IOException e) {
			flash("error", "Cannot store files");
			e.printStackTrace();
		}

		String input = d.getStringContent();
		 */

		CustomerComputation computation = new CustomerComputation(request().remoteAddress(),"Image Processing Test","test",function,dataID.toString());

		ComputationManager.getInstance().runCustomerComputation(computation);

		return redirect("/rt/" + computation.computationID.toString());

	}


	public Result primeTest(Long input) {

		CustomerComputation custComputation = new CustomerComputation(request().remoteAddress(), "Prime Computation Test", "Prime Computation Test", "PrimeComputation", input.toString());

		ComputationManager.getInstance().runCustomerComputation(custComputation);

		return ok(ComputationNotifier.track(custComputation));
	}

	public Result retrieve(String dataID) {

		Data d = Ebean.find(Data.class,UUID.fromString(dataID));

		if (d == null) {
			return badRequest();
		}

		return ok(d.getContent());
	}

}
