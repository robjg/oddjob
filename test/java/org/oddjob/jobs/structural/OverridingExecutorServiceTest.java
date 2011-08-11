package org.oddjob.jobs.structural;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;

public class OverridingExecutorServiceTest extends TestCase {

	public static class ExecutorProvider {
		
		private int threads = 1;
		
		private ExecutorService service;
		
		public void start() {
			if (service != null) {
				throw new IllegalStateException("Service already running");
			}
			service = Executors.newFixedThreadPool(threads);
		}
		
		public void stop() throws InterruptedException, FailedToStopException {
			if (service == null) {
				throw new IllegalStateException("Service not running");
			}
			service.shutdownNow();
			if (!service.awaitTermination(10, TimeUnit.SECONDS)) {
				throw new FailedToStopException(this, "Executor Service failed to terminate within 10 seconds.");
			}
			service = null;
		}
		
		public int getThreads() {
			return threads;
		}

		public void setThreads(int threads) {
			this.threads = threads;
		}

		public ExecutorService getService() {
			return service;
		}
	}
	
	public void testExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/OverridingExecutorService.xml", getClass().getClassLoader()));
		
		oddjob.load();
		
		StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING);
		
		Thread t = new Thread(oddjob);
		t.start();

		oddjobStates.checkWait();
				
		oddjobStates.startCheck(ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.READY);
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		WaitJob wait1 = lookup.lookup("wait1", WaitJob.class); 
		WaitJob wait2 = lookup.lookup("wait2", WaitJob.class); 
		WaitJob wait3 = lookup.lookup("wait3", WaitJob.class); 
		WaitJob wait4 = lookup.lookup("wait4", WaitJob.class); 
		
		StateSteps states1 = new StateSteps(wait1);
		StateSteps states2 = new StateSteps(wait2);
		StateSteps states3 = new StateSteps(wait3);
		StateSteps states4 = new StateSteps(wait4);
		
		states1.startCheck(JobState.READY, JobState.EXECUTING);
		states2.startCheck(JobState.READY, JobState.EXECUTING);
		states3.startCheck(JobState.READY, JobState.EXECUTING);
		states4.startCheck(JobState.READY, JobState.EXECUTING);

		states1.checkWait();
		states2.checkWait();
		states3.checkWait();
		states4.checkWait();
		
//		states1.startCheck(JobState.EXECUTING);
//		states2.startCheck(JobState.EXECUTING, JobState.COMPLETE);
//		states3.startCheck(JobState.READY, JobState.EXECUTING);
//		states4.startCheck(JobState.READY);
//		
//		wait2.stop();
//		
//		states1.checkWait();
//		states2.checkWait();
//		states3.checkWait();
//		states4.checkWait();
		
		states1.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		states2.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		states3.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		states4.startCheck(JobState.EXECUTING, JobState.COMPLETE);
		
		oddjob.stop();
		
		oddjobStates.checkNow();
		
		states1.checkNow();
		states2.checkNow();
		states3.checkNow();
		states4.checkNow();
		
		oddjob.destroy();
	}
}
