package org.oddjob;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.OddjobRunner.ExitHandler;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.Service;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

public class OddjobRunnerTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(OddjobRunnerTest.class);
	
	String timeoutProperty;
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("-----------------  " + getName() + "  ---------------------");
		
		timeoutProperty = System.getProperty(
				OddjobRunner.KILLER_TIMEOUT_PROPERTY);
	}

    @After
    public void tearDown() throws Exception {

		
		if (timeoutProperty == null) {
			System.getProperties().remove(
					OddjobRunner.KILLER_TIMEOUT_PROPERTY);
		}
		else {
			System.setProperty(OddjobRunner.KILLER_TIMEOUT_PROPERTY, 
					timeoutProperty);
		}
	}
	
	public static class OurService implements Service {
		
		volatile boolean started;
		
		@Override
		public void start() throws Exception {
			if (started) {
				throw new IllegalStateException();
			}
			started = true;
		}
		
		@Override
		public void stop() throws FailedToStopException {
			if (!started) {
				throw new IllegalStateException();
			}
			started = false;
		}
		
		public boolean isStarted() {
			return started;
		}
	}
	
   @Test
	public void testOddjobDestroyedFromShutdownHook() throws Exception {
		
		System.setProperty(OddjobRunner.KILLER_TIMEOUT_PROPERTY, 
				new Long(Long.MAX_VALUE).toString());
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean id='service' class='" + OurService.class.getName() + "'/>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oddjob.load();
		
		Object serviceProxy = new OddjobLookup(oddjob).lookup("service");
		
		OddjobRunner test = new OddjobRunner(oddjob, new ExitHandler() {
			@Override
			public void exit(int exitStatus) {
				throw new RuntimeException("Unexpected!");
			}
		});
		
		final StateSteps serviceStates = new StateSteps((Stateful) serviceProxy);
		
		final StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		final Runnable shutdownHook = test.new ShutdownHook();
		
		final AtomicReference<Exception> ex = new AtomicReference<>();
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					oddjobStates.checkWait();
					
					oddjobStates.startCheck(ParentState.STARTED,
							ParentState.COMPLETE, ParentState.DESTROYED);
					
					serviceStates.startCheck(ServiceState.STARTED, 
							ServiceState.STOPPED, ServiceState.DESTROYED);
					
					shutdownHook.run();
					
				} catch (Exception e) {
					logger.error("Shutdown hook threw unexpected exception.", e);
					ex.set(e);
				}
			}
		});
		t.start();
		
		test.run();
		
		t.join(OddjobTestHelper.TEST_TIMEOUT);
		
		serviceStates.checkNow();
		
		oddjobStates.checkNow();
		
		assertNull("Not expected Shutdown Hook exception", ex.get());
	}
	
	public static class FailsToDestroy  extends SimpleJob {
		
		@Override
		protected int execute() throws Throwable {
			sleep(Long.MAX_VALUE);
			return 0;
		}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			try {
				Thread.sleep(Long.MAX_VALUE);
			} 
			catch (InterruptedException e) {
				// Expected
			}
		}
	}
	
   @Test
	public void testTimeoutKiller() throws Exception {
		
		System.setProperty(OddjobRunner.KILLER_TIMEOUT_PROPERTY, "1");
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean id='slow' class='" + FailsToDestroy.class.getName() + "'/>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oddjob.load();
		
		Object slowToDestroy = new OddjobLookup(oddjob).lookup("slow");
		
		final Thread mainThread = Thread.currentThread();
		
		OddjobRunner test = new OddjobRunner(oddjob, new ExitHandler() {
			@Override
			public void exit(int exitStatus) {
				assertEquals(-1, exitStatus);
				mainThread.interrupt();
				logger.info("Killer thread complete");
			}
		});
		
		final StateSteps slowStates = new StateSteps(
				(Stateful) slowToDestroy);
		slowStates.startCheck(JobState.READY, JobState.EXECUTING);
		
		final Runnable shutdownHook = test.new ShutdownHook();
		
		final AtomicReference<Exception> ex = new AtomicReference<>();
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					slowStates.checkWait();
										
					shutdownHook.run();
					
					logger.info("Shutdown hook complete");
					
				} catch (InterruptedException e) {
					ex.set(e);
				}
			}
		});
		
		t.start();
		
		test.run();
		
		t.join(OddjobTestHelper.TEST_TIMEOUT);
		
		if (ex.get() != null) {
			throw ex.get();
		}
	}
}
