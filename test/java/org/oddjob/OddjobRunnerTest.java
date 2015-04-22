package org.oddjob;

import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import org.oddjob.OddjobRunner.ExitHandler;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.Service;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

public class OddjobRunnerTest extends TestCase {

	String timeoutProperty;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		timeoutProperty = System.getProperty(
				OddjobRunner.KILLER_TIMEOUT_PROPERTY);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
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
							ParentState.DESTROYED);
					
					serviceStates.startCheck(ServiceState.STARTED, 
							ServiceState.STOPPED, ServiceState.DESTROYED);
					
					shutdownHook.run();
					
				} catch (Exception e) {
					ex.set(e);
				}
			}
		});
		t.start();
		
		test.run();
		
		t.join(OddjobTestHelper.TEST_TIMEOUT);
		
		serviceStates.checkNow();
		
		oddjobStates.checkNow();
		
		if (ex.get() != null) {
			throw ex.get();
		}
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
		
		final AtomicReference<Thread> shutdownThread = 
				new AtomicReference<>();;
		
		OddjobRunner test = new OddjobRunner(oddjob, new ExitHandler() {
			@Override
			public void exit(int exitStatus) {
				assertEquals(-1, exitStatus);
				shutdownThread.get().interrupt();
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
										
					shutdownThread.set(Thread.currentThread());
					
					shutdownHook.run();
					
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
