package controllers;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import java.util.Map;

import models.CustomerComputation;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import twork.ComputationManager;

public class Web extends Controller{

	public Result index() {
		List<String> l = (List) Arrays.asList("Prime Computation","Image Processing");
		return ok(views.html.main.render("test", l, new play.twirl.api.Html("something")));
	}


	public Result upload() {
		RequestBody body = request().body();
		play.mvc.Http.MultipartFormData fileBody = body.asMultipartFormData();
		String immediateInput = fileBody.asFormUrlEncoded().get("input")[0];
		String computationID = "1";

		assert(immediateInput!=null);

		List<FilePart> files = fileBody.getFiles();
		for (FilePart filePart : files) {
			if (filePart != null) {
				String filename = filePart.getFilename();
				String contentType = filePart.getContentType();
				File file = filePart.getFile();
				return ok("File uploaded " + immediateInput +" "+computationID);
			}
			else {
				flash("error", "Missing file");
				return ok();
			}
		}
		return ok();
	}


	public Result primeTest(Long input) {
		//CustomerComputation custComputation = new CustomerComputation(request().remoteAddress(), "Prime Computation Test", "Prime Computation Test", "PrimeComputation", input.toString());
		//ComputationManager.getInstance().runCustomerComputation(custComputation);
	
		
		//Bad polling
		//while (custComputation.status != CustomerComputation.RUNNING){			
		//}
		
		return ok(input.toString());
	}
		
		
		
}
