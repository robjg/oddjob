package org.oddjob;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.Service;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
				Long.toString(Long.MAX_VALUE));
		
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
		
		OddjobRunner test = new OddjobRunner(oddjob, exitStatus -> {
			throw new RuntimeException("Unexpected!");
		});
		
		final StateSteps serviceStates = new StateSteps((Stateful) serviceProxy);
		
		final StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		final Runnable shutdownHook = test.new ShutdownHook();
		
		final AtomicReference<Exception> ex = new AtomicReference<>();
		
		Thread t = new Thread(() -> {
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
		});
		t.start();
		
		test.run();
		
		t.join(OddjobTestHelper.TEST_TIMEOUT);
		
		serviceStates.checkNow();
		
		oddjobStates.checkNow();
		
		assertNull("Not expected Shutdown Hook exception", ex.get());
	}
	
	public static class FailsToDestroy  extends SimpleJob implements Stoppable {

		BlockingQueue<String> queue = new LinkedBlockingQueue<>();

		@Override
		protected int execute() throws InterruptedException {
			logger().info("Blocking till stopped");
			if (queue.poll(5, TimeUnit.SECONDS) == null) {
				throw new IllegalStateException("Not Stopped");
			}
			return 0;
		}

		@Override
		protected void onStop() throws FailedToStopException {
			logger().info("Unblocking with stop");
			queue.add("Stop");
		}

		@Override
		protected void onDestroy() {
			super.onDestroy();
			try {
				Thread.sleep(5000L);
				throw new IllegalStateException("Should be interrupted by Killer thread");
			} 
			catch (InterruptedException e) {
				// Expected
			}
		}
	}
	
   @Test
	public void testTimeoutKiller() throws Exception {
		
		System.setProperty(OddjobRunner.KILLER_TIMEOUT_PROPERTY, "500");
		
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
		
		OddjobRunner test = new OddjobRunner(oddjob, exitStatus -> {
			assertEquals(-1, exitStatus);
			mainThread.interrupt();
			logger.info("Killer thread complete");
		});
		
		final StateSteps slowStates = new StateSteps(
				(Stateful) slowToDestroy);
		slowStates.startCheck(JobState.READY, JobState.EXECUTING);
		
		final Runnable shutdownHook = test.new ShutdownHook();
		
		final AtomicReference<Exception> ex = new AtomicReference<>();
		
		Thread t = new Thread(() -> {
			try {
				slowStates.checkWait();

				shutdownHook.run();

				logger.info("Shutdown hook complete");

			} catch (InterruptedException e) {
				ex.set(e);
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
