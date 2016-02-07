package test;

import static org.junit.Assert.*;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

import java.util.List;
import java.util.UUID;

import models.Computation;
import models.Job;

import org.junit.Test;

import com.avaje.ebean.Ebean;
import computations.PrimeComputation;

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


				ComputationManager cm = ComputationManager.getInstance();

				assertEquals("Empty CM is empty", 0, cm.getNumberOfComputations());

				cm.addBasicComputation(new PrimeComputation(), "5");

				assertEquals("CM can add computation", 1, cm.getNumberOfComputations());
				
				assertTrue("Prime adds jobs", Ebean.find(Job.class).findList().size() > 0);
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
				js.rebuild();

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
