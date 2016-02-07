package test;

import static org.junit.Assert.*;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import models.CompletedComputation;
import models.Computation;
import models.Data;
import models.Job;

import org.junit.Test;

import com.avaje.ebean.Ebean;

import computations.ComputationCode;
import computations.PrimeComputation;
import computations.PrimeComputationCode;

import twork.ComputationManager;
import twork.Device;
import twork.JobScheduler;
import twork.Logger;


/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

	@Test
	public void simpleCheck() {
		int a = 1 + 1;
		assertEquals(2, a);
	}

	@SuppressWarnings("unused")
	@Test
	public void Device_BasicTest() {
		Device d = new Device(1L);
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
				assertTrue(jl.isEmpty());
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
	public void CM_Test() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Logger.enable = true;
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
	
	public String genericPrimeTest(final long prime) {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Logger.enable = false;
				Ebean.delete(Ebean.find(CompletedComputation.class).findList());
				Device d = new Device(1L);
				ComputationManager cm = ComputationManager.getInstance();
				JobScheduler js = JobScheduler.getInstance();
				cm.addBasicComputation(new PrimeComputation(), Long.toString(prime));

				Job primeJob;
				while((primeJob = js.getJob(d)) != null) {
					ComputationCode cc = new PrimeComputationCode();
					//TODO: Data dependence
					Data inData = Ebean.find(Data.class, primeJob.intputDataID);
					String jobInput = inData.getContent();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					InputStream in = new ByteArrayInputStream(jobInput.getBytes(StandardCharsets.UTF_8));
					cc.run(in, out);
					String outString = new String(out.toByteArray(), StandardCharsets.UTF_8);
					js.submitJob(d, outString);
				}
				result = cm.getCompletedComputations().get(0).output;
			}
		});

		return result;
	}

	@Test
	public void Prime_CorrectnessTest() {
		//5 is prime
		String output = genericPrimeTest(5);
		assertEquals("Prime computation run on 5", output, "No factor found for 5.");
		
		//21 = 7 * 3
		output = genericPrimeTest(21);
		assertTrue("Prime computation run on 21", output.equals("Found factor for 21: 3.") || output.equals("Found factor for 21: 7."));
		
		//29 is prime
		output = genericPrimeTest(29);
		assertEquals("Prime computation run on 29", output, "No factor found for 29.");
		
		//7919 is prime
		output = genericPrimeTest(7919);
		assertEquals("Prime computation run on 7919", output, "No factor found for 7919.");
		
		//373987259 = 3571 * 104729
		output = genericPrimeTest(373987259);
		assertTrue("Prime computation run on 373987259", output.equals("Found factor for 373987259: 3571.") || output.equals("Found factor for 373987259: 104729."));
	}

	@Test
	public void Prime_DetailedTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Logger.enable = false;
				Device d = new Device(1L);
				ComputationManager cm = ComputationManager.getInstance();
				cm.rebuild_TEST();
				JobScheduler js = JobScheduler.getInstance();
				js.rebuild_TEST();

				//Make new computation
				cm.addBasicComputation(new PrimeComputation(), "4");
				assertEquals("Prime(4) has 1 job", 1, Ebean.find(Job.class).findList().size());

				//Get its job
				Job primeJob = js.getJob(d);
				assertNotNull(primeJob);

				//Run it
				ComputationCode cc = new PrimeComputationCode();
				//TODO: Data dependance
				Data inData = Ebean.find(Data.class, primeJob.intputDataID);
				assertNotNull("Prime job has associated data", inData);

				String jobInput = inData.getContent();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStream in = new ByteArrayInputStream(jobInput.getBytes(StandardCharsets.UTF_8));

				cc.run(in, out);

				String outString = new String(out.toByteArray(), StandardCharsets.UTF_8);

				assertEquals("Prime can find out that 2|4", "2", outString);

				assertTrue("No completed Computations", cm.getCompletedComputations().isEmpty());

				//Submit it
				js.submitJob(d, outString);
				assertEquals("JS has removed completed job", 0, js.getNumberOfActiveJobs());

				//Get result				
				List<CompletedComputation> completedComputations = cm.getCompletedComputations();

				assertEquals("One completed computation", 1, completedComputations.size());

				assertEquals("Check Prime output for 4", "Found factor for 4: 2.", completedComputations.get(0).output);

				assertEquals("CM computation has been removed", 0, cm.getNumberOfComputations());
				assertTrue("Completed job has been deleted", Ebean.find(Job.class).findList().isEmpty());

				//TODO: Finish this (take both jobs at once)
				/*
				//Make new Computation
				cm.addBasicComputation(new PrimeComputation(), "5");
				assertEquals("Prime(5) has 2 jobs", 2, Ebean.find(Job.class).findList().size());

				//Get the jobs
				Job primeJob1 = js.getJob(d);
				assertNotNull(primeJob1);
				Job primeJob2 = js.getJob(d);
				assertNotNull(primeJob2);

				inData = Ebean.find(Data.class, primeJob1.intputDataID);
				assertNotNull("Prime job has associated data", inData);

				jobInput = inData.getContent();
				out = new ByteArrayOutputStream();
				in = new ByteArrayInputStream(jobInput.getBytes(StandardCharsets.UTF_8));

				cc.run(in, out);

				String outString = new String(out.toByteArray(), StandardCharsets.UTF_8);
				 */
			}
		});
	}

	@Test
	public void JS_FullTest() {
		running(fakeApplication(inMemoryDatabase()), new Runnable() {
			public void run() {
				Logger.enable = false;
				JobScheduler js = JobScheduler.getInstance();
				Device d = new Device(1L);

				//It can't have any Jobs yet.
				assertEquals("Empty JS returns null", js.getJob(d), null);

				//Add a job
				Computation c = new Computation("Title", "Description");
				c.running = true;
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
				assertNull("JS failes jobs that repeatedly time out", k);


				//Check the job has been destroyed or marked as failed
				k = Ebean.find(Job.class, jID);
				assertTrue(k == null || k.failed);

				//Make a new setup
				c = new Computation("Title", "Description");
				c.running = true;
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
				assertNull(js.getJob(d));
				assertTrue("JS Handling multiple jobs", l.jobID.equals(j.jobID) || l.jobID.equals(o.jobID));
				assertTrue("JS Handling multiple jobs", m.jobID.equals(j.jobID) || m.jobID.equals(o.jobID));
				assertFalse("JS Handling multiple jobs", l.jobID.equals(m.jobID));
				js.submitJob(d, "result");
				l = Ebean.find(Job.class, j.jobID);
				m = Ebean.find(Job.class, o.jobID);
				assertTrue("JS Adding data to jobs", m.outputDataID != Device.NULL_UUID);
			}
		});
	}


}
