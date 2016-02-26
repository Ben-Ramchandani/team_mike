package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import models.Computation;
import models.CustomerComputation;
import models.Data;
import models.Device;
import models.Device.TimeoutJob;
import models.Job;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import twork.ComputationManager;
import twork.Devices;
import twork.JobScheduler;
import twork.MyLogger;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import computations.ComputationCode;
import computations.EdgeDetect;
import computations.PrimeComputation;
import computations.PrimeComputationCodeInternal;


public class ApplicationTest {

	@Test
	public void web_test() {
		
		running(testServer(9001, fakeApplication(inMemoryDatabase())), new Runnable() {
			public void run() {
				try {
					MyLogger.enable = false;
					MyLogger.log("Checking web connection");

					String urlString = "http://localhost:9001/";

					//Send GET /available
					URL webURL = new URL(urlString);

					HttpURLConnection con = (HttpURLConnection) webURL.openConnection();
					con.connect();

					//Check the response
					assertEquals("Website gives 200 response code", 200, con.getResponseCode());
				} catch (Exception e) {
					System.out.println("Exceptio caught in web_test.");
					e.printStackTrace();
					throw new RuntimeException();
				}
			}
		});
	}

	@Test
	public void image_test() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				try {
					MyLogger.enable = false;
					MyLogger.log("Starting image_test");

					Device d = new Device("1");
					ComputationManager cm = ComputationManager.getInstance();
					JobScheduler js = JobScheduler.getInstance();
					assertEquals("No jobs in scheduler.", 0, js.getNumberOfJobs());


					File f = new File("test/example_image.png");
					byte[] rawImage = Files.readAllBytes(f.toPath());
					File newPath = new File("test/tmp.png");

					try {
						Files.copy(f.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						System.out.println("Failed to copy example image.");
						e.printStackTrace();
						throw new RuntimeException();
					}

					UUID dataID = Data.store(newPath);

					//Make new computation
					CustomerComputation custComputation = new CustomerComputation("Example Jones", "image test", "", "EdgeDetect", dataID.toString());

					cm.runCustomerComputation(custComputation.customerComputationID);

					assertEquals("One job in scheduler.", 1, js.getNumberOfJobs());

					Job imageJob = js.getJob(d);
					assertNotNull("Job is not null", imageJob);

					//Run it
					ComputationCode cc = new EdgeDetect();
					//TODO: Data dependence
					Data inData = Ebean.find(Data.class, imageJob.inputDataID);
					assertNotNull("Image job has associated data", inData);


					InputStream in = new ByteArrayInputStream(inData.getContent());
					byte[] data = IOUtils.toByteArray(in);
					assertTrue("Image does not change going through conversion", Arrays.equals(data, rawImage));


					ByteArrayOutputStream out = new ByteArrayOutputStream();
					in = new ByteArrayInputStream(inData.getContent());
					cc.run(in, out);

					ByteArrayInputStream res = new ByteArrayInputStream(out.toByteArray());
					BufferedImage i = ImageIO.read(res);
					assertNotNull("Data is still image after job code", i);


				} catch (Exception e) {
					System.out.println("Exception caught in image_test.");
					e.printStackTrace();
					throw new RuntimeException();
				}
			}
		});
	}

	@Test
	public void deletion_test() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Computation c = new Computation("Title", "Description");
				c.save();
				c.delete();
				c = new Computation("Title", "Description");
				c.save();
				Job j = new Job(c, "des", UUID.randomUUID(), "func");
				c.jobs.add(j);
				j.save();
				c.save();
				c.delete();
				List<Job> jl = Ebean.find(Job.class).findList();
				assertTrue("Computation deletes cascade to jobs", jl.isEmpty());
			}
		});
	}

	@Test
	public void customer_computation_order_test() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				new CustomerComputation("Example James", "firstName", "desc", "function", "input");
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {}
				new CustomerComputation("Example James", "secondName", "desc", "function", "input");
				List<CustomerComputation> ccs = ComputationManager.getInstance().getComputationsByCustomerName("Example James");
				assertEquals("There are two customerComputations under \"Example James\".", 2, ccs.size());
				CustomerComputation c = ccs.get(0);
				//Should have most recent first
				assertEquals("Most recent CustomerComputation is returned first", "firstName", c.computationName);
			}
		});
	}



	public void clear_db() {
		Ebean.delete(Ebean.find(Computation.class).findList());
		Ebean.delete(Ebean.find(CustomerComputation.class).findList());
		Ebean.delete(Ebean.find(Job.class).findList());
	}


	//Test disabled because it takes ages
	/*
	@Test
	public void session_timeout_test() {
		running(testServer(9001), new Runnable() {
			public void run() {
				try{
				String urlString = "http://localhost:9001/";

				//Send GET /available
				URL availableURL = new URL(urlString + "available");
				HttpURLConnection con = (HttpURLConnection) availableURL.openConnection();
				con.connect();
				assertEquals("Available gives 200 response code", 200, con.getResponseCode());
				String cookie = con.getHeaderField("Set-Cookie");
				assertNotNull("Available returns a cookie", cookie);

				//Sleep for a bit
				Thread.sleep(1000);


				//Send GET /job with cookie - expect NO JOB
				URL jobURL = new URL(urlString + "job");
				HttpURLConnection jobCon1 = (HttpURLConnection) jobURL.openConnection();
				jobCon1.setRequestProperty("Cookie", cookie);
				jobCon1.connect();

				assertEquals("GET /job with cookie after 1 second returns 555 - No Job", 555, jobCon1.getResponseCode());


				//Sleep for a bit more
				Thread.sleep(60000);
				//Send GET /job with cookie - expect NO JOB
				HttpURLConnection jobCon2 = (HttpURLConnection) jobURL.openConnection();
				jobCon2.setRequestProperty("Cookie", cookie);
				jobCon2.connect();

				assertEquals("GET /job with cookie after 1 minute returns 555 - No Job", 555, jobCon2.getResponseCode());

				} catch(Throwable t) {
					t.printStackTrace();
					assertTrue("Exception", false);
					return;
				}
			}
		});
	}
	 */
	
	@Test
	public void available_test() {
		MyLogger.enable = false;
		running(testServer(9001, fakeApplication(inMemoryDatabase())), new Runnable() {
			public void run() {
				try {
					MyLogger.log("Starting available test.");

					String urlString = "http://localhost:9001/";
					
					int n = Devices.getInstance().getNumberOfActiveDevices();

					//Send POST /available with ID given
					URL availableURL = new URL(urlString + "available");
					HttpURLConnection con = (HttpURLConnection) availableURL.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("content-type", "text/plain");
					con.setDoOutput(true);
					OutputStream osw = con.getOutputStream();
					osw.write("{phone-id: \"5\"}".getBytes(StandardCharsets.UTF_8));
					osw.close();
					
					assertEquals("Available with a phone-id returns 200 - OK", 200, con.getResponseCode());
					
					//Send POST /available with same ID (no cookie)
					con = (HttpURLConnection) availableURL.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("content-type", "text/plain");
					con.setDoOutput(true);
					osw = con.getOutputStream();
					osw.write("{phone-id: \"5\"}".getBytes(StandardCharsets.UTF_8));
					osw.close();
					String cookie = con.getHeaderField("Set-Cookie");
					
					
					assertEquals("Available with a phone-id returns 200 - OK", 200, con.getResponseCode());
					assertEquals("Only one device has been created for two available requests with the same ID but no cookie",
									n+1, Devices.getInstance().getNumberOfActiveDevices());
					
					//Send POST /available with same ID and cookie
					con = (HttpURLConnection) availableURL.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("content-type", "text/plain");
					con.setRequestProperty("Cookie", cookie);
					con.setDoOutput(true);
					osw = con.getOutputStream();
					osw.write("{phone-id: \"5\"}".getBytes(StandardCharsets.UTF_8));
					osw.close();
					
					assertEquals("Available with a phone-id and cookie returns 200 - OK", 200, con.getResponseCode());
					assertEquals("Only one device has been created after available requests with the same ID and cookie",
									n+1, Devices.getInstance().getNumberOfActiveDevices());
					
					//Send POST /available with only cookie
					con = (HttpURLConnection) availableURL.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Cookie", cookie);
					con.connect();
					
					assertEquals("Available with a cookie returns 200 - OK", 200, con.getResponseCode());
					assertEquals("Only one device has been created after available requests with just the cookie",
									n+1, Devices.getInstance().getNumberOfActiveDevices());
					
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue("Exception", false);
					return;
				}
			}
		});
	}
	
	
	@Test
	public void full_test() {
		MyLogger.enable = false;
		running(testServer(9001, fakeApplication(inMemoryDatabase())), new Runnable() {
			public void run() {
				try {

					MyLogger.log("Starting full test.");

					String urlString = "http://localhost:9001/";

					//Send GET /available
					URL availableURL = new URL(urlString + "available");
					HttpURLConnection con = (HttpURLConnection) availableURL.openConnection();
					con.connect();

					//Check the response
					assertEquals("Available gives 200 response code", 200, con.getResponseCode());
					String cookie = con.getHeaderField("Set-Cookie");
					assertNotNull("Available returns a cookie", cookie);


					//Send GET /job with no cookie - expect unauthorised.
					URL jobURL = new URL(urlString + "job");
					HttpURLConnection jobCon1 = (HttpURLConnection) jobURL.openConnection();
					jobCon1.connect();

					assertEquals("GET /job with no cookie returns 401 - Unauthorized", 401, jobCon1.getResponseCode());



					//Send GET /job with cookie - expect NO JOB
					HttpURLConnection jobCon2 = (HttpURLConnection) jobURL.openConnection();
					jobCon2.setRequestProperty("Cookie", cookie);
					jobCon2.connect();

					assertEquals("GET /job with cookie returns 204 - No Content", 204, jobCon2.getResponseCode());


					//Add a job
					URL addComputationURL = new URL(urlString + "test/add/" + "John_Smith" + "/" + "4");
					HttpURLConnection addCon = (HttpURLConnection) addComputationURL.openConnection();
					addCon.setRequestMethod("POST");
					addCon.connect();
					assertEquals("POST /test/add... returns 200 - OK", 200, addCon.getResponseCode());



					//Send GET /job with cookie - expect a job
					HttpURLConnection jobCon3 = (HttpURLConnection) jobURL.openConnection();
					jobCon3.setRequestProperty("Cookie", cookie);
					jobCon3.connect();

					assertEquals("GET /job with cookie returns 200 - OK", 200, jobCon3.getResponseCode());


					InputStream in = jobCon3.getInputStream();
					StringWriter writer = new StringWriter();
					IOUtils.copy(in, writer, StandardCharsets.UTF_8);
					String str = writer.toString();
					JsonNode jn = (new ObjectMapper()).readTree(str);

					long jobID = jn.get("job-id").asLong();			

					String functionName = jn.get("function-class").asText();
					assertNotNull("/job reply has funciton-class", functionName);


					//Send GET /code/:jobID
					URL codeURL = new URL(urlString + "code/" + functionName);
					HttpURLConnection codeCon = (HttpURLConnection) codeURL.openConnection();
					codeCon.setRequestProperty("Cookie", cookie);
					codeCon.connect();

					assertEquals("GET /code/:functionName returns 200 - OK", 200, codeCon.getResponseCode());

					/*
					//Fetch and instantiate the class
					URLClassLoader loader = new URLClassLoader(new URL[] {new URL(urlString + "code/")});
					 */



					TerribleURLClassLoader loader = new TerribleURLClassLoader(new URL(urlString + "test/code/"));
					Class<?> codeClass = loader.loadClass(functionName);
					Object o = codeClass.newInstance();
					Method codeToRun = codeClass.getDeclaredMethod("run", new Class<?>[] {InputStream.class, OutputStream.class});

					//Get data
					URL dataURL = new URL(urlString + "data/" + Long.toString(jobID));
					HttpURLConnection dataCon = (HttpURLConnection) dataURL.openConnection();
					dataCon.setRequestProperty("Cookie", cookie);
					dataCon.connect();

					assertEquals("GET /data/:jobID returns 200 - OK", 200, dataCon.getResponseCode());
					InputStream jobInput = dataCon.getInputStream();
					ByteArrayOutputStream jobOutput = new ByteArrayOutputStream();

					//Run the job
					codeToRun.invoke(o, jobInput, jobOutput);

					//Check output
					String outStr = new String(jobOutput.toByteArray(), StandardCharsets.UTF_8);
					assertEquals("Output of Prime(4) job is \"2\"", "2", outStr);


					//Send result back
					URL resultURL = new URL(urlString + "result/" + Long.toString(jobID));
					HttpURLConnection resultCon = (HttpURLConnection) resultURL.openConnection();
					resultCon.setRequestProperty("Cookie", cookie);
					resultCon.setRequestMethod("POST");
					resultCon.setRequestProperty("content-type", "text/plain");
					resultCon.setDoOutput(true);

					OutputStream osw = resultCon.getOutputStream();
					osw.write(outStr.getBytes(StandardCharsets.UTF_8));
					osw.close();

					assertEquals("POST /result/:jobID returns 200 - OK", 200, resultCon.getResponseCode());


					ComputationManager cm = ComputationManager.getInstance();
					List<CustomerComputation> comps = cm.getComputationsByCustomerName("John_Smith");
					assertEquals("One customer computation for John Smith", 1, comps.size());
					assertEquals("Correct output from full test", comps.get(0).output, "Found factor for 4: 2.");

					MyLogger.log("End full test.");
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue("Exception", false);
					return;
				}
			}
		});
	}


	@Test
	public void timeout_test() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				//Set up a job as normal.
				MyLogger.enable = false;
				MyLogger.log("Starting timeout_test");
				Device d = new Device("1");
				ComputationManager cm = ComputationManager.getInstance();
				JobScheduler js = JobScheduler.getInstance();
				CustomerComputation custComputation = new CustomerComputation("Example name", "Prime (timeout_test)", "", "PrimeComputation", "4");
				cm.runCustomerComputation(custComputation);
				Job primeJob = js.getJob(d);
				assertNotNull("Job taken from JS", primeJob);

				//That should be the only job
				assertNull("One job is gone", js.getJob(d));

				//Mimic Device.registerJob
				d.currentJob = primeJob.jobID;

				//Force a timeout
				TimeoutJob t = new Device.TimeoutJob(d);
				t.run();

				//Job should be back in scheduler
				assertNotNull("Job should be back in scheduler", js.getJob(d));
			}
		});
	}



	@Test
	public void UUID_BasicTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Computation c = new Computation("Title", "Description");
				c.save();
				assertNotNull(c.computationID);
				Job j = new Job(c, "", Device.NULL_UUID, "");
				j.save();
				assertNotNull(j.computationID);
				assertEquals(j.computationID, c.computationID);
				assertEquals(j.parentComputation, c);
			}
		});
	}


	@Test
	public void Job_BasicTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Computation c = new Computation("Title", "Description");
				c.save();
				Job j = new Job(c, "A job", UUID.randomUUID(), "a function");
				j.save();
				Ebean.find(Job.class);
				List<Job> g = Ebean.find(Job.class).findList();
				assertEquals(g.size(), 1);
				assertEquals(g.get(0), j);
				assertEquals(j.outputDataID, Device.NULL_UUID);
			}
		});
	}


	@Test
	public void function_list_test() {
		MyLogger.enable = false;
		running(testServer(9001, fakeApplication(inMemoryDatabase())), new Runnable() {
			public void run() {
				try {
					MyLogger.log("Starting function list test.");
					String urlString = "http://localhost:9001/";
					
					String expectedString = "{\"computations\":[{\"name\":\"Prime checking\",\"description\":\"Work out if a given number is prime.\",\"id\":\"PrimeComputationCode\"}," + 
								"{\"name\":\"Image manipulation\",\"description\":\"Do something to images.\",\"id\":\"EdgeDetect\"}]}";

					//Send GET /computations
					URL computationsURL = new URL(urlString + "computations");
					HttpURLConnection con = (HttpURLConnection) computationsURL.openConnection();
					con.connect();

					//Check the response
					assertEquals("Available gives 200 response code", 200, con.getResponseCode());
					
					assertEquals("Function list is correct", expectedString, IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8));
					
					
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue("Exception", false);
					return;
				}
			}
		});
	}

	@Test
	public void CM_Test() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				MyLogger.enable = true;
				ComputationManager cm = ComputationManager.getInstance();
				cm.rebuild_TEST();

				assertEquals("Empty CM is empty", 0, cm.getNumberOfComputations());

				cm.addBasicComputation(new PrimeComputation(), "5");

				assertEquals("CM can add computation", 1, cm.getNumberOfComputations());

				assertTrue("Prime adds jobs", Ebean.find(Job.class).findList().size() > 0);
			}
		});
	}

	public String result;

	public String genericPrimeTest(final long prime, final String name) {
		Device d = new Device("1");
		ComputationManager cm = ComputationManager.getInstance();
		JobScheduler js = JobScheduler.getInstance();
		CustomerComputation custComputation = new CustomerComputation(name, "Prime generic test", "", "PrimeComputation", Long.toString(prime));
		cm.runCustomerComputation(custComputation);

		Job primeJob;
		while((primeJob = js.getJob(d)) != null) {
			ComputationCode cc = new PrimeComputationCodeInternal();
			//TODO: Data dependence
			Data inData = Ebean.find(Data.class, primeJob.inputDataID);
			String jobInput = inData.getContentAsString();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = new ByteArrayInputStream(jobInput.getBytes(StandardCharsets.UTF_8));
			cc.run(in, out);
			js.submitJob(d, out.toByteArray());
		}
		result = cm.getComputationsByCustomerName(name).get(0).output;


		return result;
	}

	@Test
	public void Prime_CorrectnessTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				MyLogger.enable = false;
				MyLogger.log("Prime_CorrectnessTest");
				Ebean.delete(Ebean.find(CustomerComputation.class).findList());
				//5 is prime
				String output = genericPrimeTest(5, "Ben");
				assertEquals("Prime computation run on 5", output, "No factor found for 5.");

				//21 = 7 * 3
				output = genericPrimeTest(21, "Razvan");
				assertTrue("Prime computation run on 21", output.equals("Found factor for 21: 3.") || output.equals("Found factor for 21: 7."));

				//29 is prime
				output = genericPrimeTest(29, "Dima");
				assertEquals("Prime computation run on 29", output, "No factor found for 29.");

				//7919 is prime
				output = genericPrimeTest(7919, "James");
				assertEquals("Prime computation run on 7919", output, "No factor found for 7919.");

				//373987259 = 3571 * 104729
				output = genericPrimeTest(373987259, "Laura");
				assertTrue("Prime computation run on 373987259", output.equals("Found factor for 373987259: 3571.") || output.equals("Found factor for 373987259: 104729."));

				assertEquals(Ebean.find(CustomerComputation.class).findList().size(), 5);
			}
		});
	}

	@Test
	public void Prime_DetailedTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				MyLogger.enable = false;
				MyLogger.log("Starting Prime_DetailedTest");

				Device d = new Device("1");
				ComputationManager cm = ComputationManager.getInstance();
				cm.rebuild_TEST();
				JobScheduler js = JobScheduler.getInstance();
				js.rebuild_TEST();

				//Make new computation
				CustomerComputation custComputation = new CustomerComputation("Example Joe", "Prime detailed test", "", "PrimeComputation", "4");

				//cm.addBasicComputation(new PrimeComputation(), "4");
				cm.runCustomerComputation(custComputation);
				assertEquals("Prime(4) has 1 job", 1, Ebean.find(Job.class).findList().size());
				assertEquals("JS has one job", 1, js.getNumberOfJobs());
				assertEquals("Both have same job", js.jobMap.keySet().toArray()[0], Ebean.find(Job.class).findList().get(0).jobID);

				//Get its job
				Job primeJob = js.getJob(d);
				assertNotNull(primeJob);

				//Run it
				ComputationCode cc = new PrimeComputationCodeInternal();
				//TODO: Data dependence
				Data inData = Ebean.find(Data.class, primeJob.inputDataID);
				assertNotNull("Prime job has associated data", inData);

				String jobInput = inData.getContentAsString();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStream in = new ByteArrayInputStream(jobInput.getBytes(StandardCharsets.UTF_8));

				cc.run(in, out);

				String outString = new String(out.toByteArray(), StandardCharsets.UTF_8);

				assertEquals("Prime can find out that 2|4", "2", outString);


				//Submit it
				js.submitJob(d, out.toByteArray());
				assertEquals("JS has removed completed job", 0, js.getNumberOfActiveJobs());

				//Get result				
				List<CustomerComputation> customerComputations = cm.getCustomerComputations();

				assertEquals("One customer computation", 1, customerComputations.size());

				assertEquals("Check Prime output for 4", "Found factor for 4: 2.", customerComputations.get(0).output);

				assertEquals("CM computation has been removed", 0, cm.getNumberOfComputations());
				assertTrue("Completed job has been deleted", Ebean.find(Job.class).findList().isEmpty());
			}
		});
	}

	@Test
	public void JS_FullTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {

				MyLogger.enable = false;
				MyLogger.log("Starting JS_FullTest");
				JobScheduler js = JobScheduler.getInstance();
				Device d = new Device("1");

				//It can't have any Jobs yet.
				assertEquals("Empty JS returns null", js.getJob(d), null);

				//Add a job
				Computation c = new Computation("Title", "Description");
				c.save();
				//The random UUID is the inputData
				Job j = new Job(c, "A job", UUID.randomUUID(), "a function");
				c.jobs.add(j);
				c.save();
				j.save();
				UUID jID = j.jobID;
				js.rebuild_TEST();

				//And expect there to be a job now
				Job g = js.getJob(d);
				assertNotNull("JS returns correct Job", g);
				assertEquals("JS returns correct Job", g, j);
				assertEquals("JS returns correct Job", g.jobID, j.jobID);
				assertEquals("JS 1 active job", js.getNumberOfActiveJobs(), 1);

				//Now the jobs is gone, it shouldn't be given out again.
				Job h = js.getJob(d);
				assertNull("JS only gives jobs once", h);

				//If the job is timed out, it should be given out again
				js.timeoutJob(jID);
				Job k = js.getJob(d);
				assertNotNull("JS gives back out failed jobs", k);
				assertEquals("JS gives back out failed jobs", k, j);
				js.timeoutJob(jID);

				//Try to submit a job hasn't been given out by the scheduler -> should fail.
				assertEquals("JS no active jobs", js.getNumberOfActiveJobs(), 0);
				Job f = Ebean.find(Job.class, jID);
				assertEquals("JS ignores jobs it doesn't know about", f.outputDataID, Device.NULL_UUID);
				k = js.getJob(d);
				assertEquals("JS returns correct Job", k, j);


				//Check the job failure mechanic
				js.timeoutJob(jID);
				k = js.getJob(d);
				
				js.timeoutJob(jID);
				k = js.getJob(d);
				
				js.timeoutJob(jID);
				k = js.getJob(d);
				
				assertNull("JS fails jobs that repeatedly time out", k);


				//Check the job has been destroyed or marked as failed
				k = Ebean.find(Job.class, jID);
				assertTrue("Job has been failed", k == null || k.failed);

				//Make a new setup
				c = new Computation("Title", "Description");
				c.save();
				j = new Job(c, "A job", UUID.randomUUID(), "a function");

				j.save();
				Job o = new Job(c, "Another job", UUID.randomUUID(), "a function");
				o.save();
				c.jobs.add(j);
				c.jobs.add(o);
				c.save();
				assertEquals("Ebean tracks job numbers", Ebean.find(Job.class).findList().size(), 2); 

				js.update();
				assertEquals("JS 2 active jobs", js.getNumberOfJobs(), 2);
				c.jobs.remove(j);
				assertEquals("Job list right length", c.jobs.size(), 1);
				j.delete();
				c.save();
				assertEquals("Ebean tracks job removal", Ebean.find(Job.class).findList().size(), 1); 
				js.update();
				assertEquals("JS update removes jobs", js.getNumberOfJobs(), 1);
				j = new Job(c, "A job", UUID.randomUUID(), "a function");
				j.save();
				js.update();
				assertEquals("JS 2 active jobs", js.getNumberOfJobs(), 2);

				//Check submitting jobs works
				Job l = js.getJob(d);
				Job m = js.getJob(d);
				assertNull("JS gives out job take two", js.getJob(d));
				assertTrue("JS Handling multiple jobs", l.jobID.equals(j.jobID) || l.jobID.equals(o.jobID));
				assertTrue("JS Handling multiple jobs", m.jobID.equals(j.jobID) || m.jobID.equals(o.jobID));
				assertFalse("JS Handling multiple jobs", l.jobID.equals(m.jobID));
				js.submitJob(d, "result".getBytes(StandardCharsets.UTF_8));
				l = Ebean.find(Job.class, j.jobID);
				m = Ebean.find(Job.class, o.jobID);
				assertTrue("JS Adding data to jobs", m.outputDataID != Device.NULL_UUID);
				MyLogger.log("Ending JS_FullTest");
			}
		});
	}

}
