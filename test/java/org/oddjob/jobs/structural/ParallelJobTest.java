package org.oddjob.jobs.structural;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.FailedToStopException;
import org.oddjob.MockStateful;
import org.oddjob.Oddjob;
import org.oddjob.OddjobComponentResolver;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.WaitJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.MockScheduledExecutorService;
import org.oddjob.scheduling.MockScheduledFuture;
import org.oddjob.state.FlagState;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.state.StateListener;

public class ParallelJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ParallelJobTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------------  " + getName()  + "  ----------------");
	}
	
	private class LaterExecutor extends MockScheduledExecutorService {

		private Runnable runnable;
		
		public Future<?> submit(Runnable runnable) {
			this.runnable = runnable;
			return new MockScheduledFuture<Void>();
		}
	}
	
	public void testStepByStepOneJob() {
		
		FlagState job1 = new FlagState(JobState.COMPLETE);

		LaterExecutor executor = new LaterExecutor();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.setExecutorService(executor);

		test.setJobs(0, job1);
		
		test.run();
	
		steps.checkNow();
		
		assertNotNull(executor.runnable);
		
		steps.startCheck(ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		executor.runnable.run();
		
		steps.checkNow();
	}
	
	public void testThreeJobs() throws InterruptedException {
	
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(1, job2);
		test.setJobs(2, job3);
		
		test.run();
	
		steps.checkWait();
		
		assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());		
		
		steps.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		test.hardReset();
		
		assertEquals(JobState.READY, job1.lastStateEvent().getState());
		assertEquals(JobState.READY, job2.lastStateEvent().getState());
		assertEquals(JobState.READY, job3.lastStateEvent().getState());
		
		steps.checkNow();
		
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		test.run();
		
		steps.checkWait();
		
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
		
		defaultServices.stop();
	}
	
	public void testThrottledExecution() throws InterruptedException {
		
		FlagState job1 = new FlagState(JobState.COMPLETE);
		FlagState job2 = new FlagState(JobState.COMPLETE);
		FlagState job3 = new FlagState(JobState.COMPLETE);

		DefaultExecutors defaultServices = new DefaultExecutors();
		defaultServices.setPoolSize(1);
		
		ParallelJob test = new ParallelJob();
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		test.setExecutorService(defaultServices.getPoolExecutor());

		test.setJobs(0, job1);
		test.setJobs(0, job2);
		test.setJobs(0, job3);
		
		test.run();
	
		steps.checkWait();		
		
		assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
		assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
	}
	
	public void testStop() throws InterruptedException, FailedToStopException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		test.setExecutorService(defaultServices.getPoolExecutor());

		WaitJob job = new WaitJob();
		
		test.setJobs(0, job);
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE);
		
		StateSteps waitState = new StateSteps(job);
		waitState.startCheck(JobState.READY, 
				JobState.EXECUTING);

		test.run();
		
		steps.checkWait();		
		waitState.checkWait();
		
		steps.startCheck( 
				ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		test.stop();
		
		steps.checkNow();		
		
		test.destroy();
		
		defaultServices.stop();
	}
	
	private class NowExecutor extends MockScheduledExecutorService {

		public Future<?> submit(Runnable runnable) {
			runnable.run();
			return new MockScheduledFuture<Void>();
		}
	}
	
	private class DestroyJob extends MockStateful 
	implements Runnable {
		
		JobStateHandler handler = new JobStateHandler(this);
		
		public void addStateListener(StateListener listener) {
			handler.addStateListener(listener);
		}
		public void removeStateListener(StateListener listener) {
			handler.removeStateListener(listener);
		}
		
		public void run() {
			handler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					handler.setState(JobState.COMPLETE);
					handler.fireEvent();
				}
			});
		}
		
		void destroy() {
			handler.waitToWhen(new IsAnyState(), new Runnable() {
				public void run() {
					handler.setState(JobState.DESTROYED);
					handler.fireEvent();
				}
			});
		}
	}
	
	/**
	 * Child state changes before null is set as the child. Parallel job
	 * shouln't get that state.
	 * @throws InterruptedException 
	 */
	public void testChildDestroyed() throws InterruptedException {
		
		ParallelJob test = new ParallelJob();
		test.setExecutorService(new NowExecutor());

		DestroyJob destroy = new DestroyJob();
		
		test.setJobs(0, destroy);
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		test.run();
		
		steps.checkWait();		
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		steps.startCheck(
				ParentState.COMPLETE, ParentState.DESTROYED);
		
		test.destroy();
		
		test.setJobs(0, null);
		
		destroy.destroy();
		
		steps.checkNow();
		
	}
		
	public void testInOddjob() throws InterruptedException {
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/structural/SimpleParallelExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		StateSteps steps = new StateSteps(oddjob);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.ACTIVE,
				ParentState.COMPLETE);		
		
		oddjob.run();		
		
		steps.checkWait();
		
		console.close();
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
		
		Set<String> results = new HashSet<String>();
		
		results.add(lines[0].trim());
		results.add(lines[1].trim());
		
		assertTrue(results.contains("This runs in parallel"));
		assertTrue(results.contains("With this which could be displayed first!"));
				
		oddjob.destroy();
	}
	
	public void testStopInOddjob() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <parallel>" +
            "   <jobs>" + 
            "    <wait id='wait1'/>" +
            "    <wait id='wait2'/>" +
            "   </jobs>" +
			"  </parallel>" +
			" </job>" + 
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.load();
		
		StateSteps oddjobState = new StateSteps(oddjob);
		
		oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE);
		
		StateSteps wait1State = new StateSteps(new OddjobLookup(oddjob).lookup(
				"wait1", Stateful.class));
		wait1State.startCheck(JobState.READY, JobState.EXECUTING);
		StateSteps wait2State = new StateSteps(new OddjobLookup(oddjob).lookup(
				"wait2", Stateful.class));
		wait2State.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(oddjob);
		t.start();
	
		wait1State.checkWait();
		wait2State.checkWait();
		
		oddjobState.checkWait();
		
		oddjobState.startCheck(ParentState.ACTIVE,
				ParentState.COMPLETE);
		
		oddjob.stop();

		oddjobState.checkNow();
		
		oddjob.destroy();
		
	}
	
	public static class MyService {
		
		public void start() {}
		public void stop() {}
	}
	
	public void testParallelServices() throws FailedToStopException, InterruptedException {
		
		DefaultExecutors defaultServices = new DefaultExecutors();
		
		ParallelJob test = new ParallelJob();
		
		test.setExecutorService(defaultServices.getPoolExecutor());
		
		Object service1 = new OddjobComponentResolver().resolve(
				new MyService(), null);
		Object service2 = new OddjobComponentResolver().resolve(
				new MyService(), null);
		
		test.setJobs(0, (Runnable) service1);
		test.setJobs(1, (Runnable) service2);
		
		StateSteps steps = new StateSteps(test);
		
		steps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE);
		
		StateSteps service1State = new StateSteps((Stateful) service1);
		service1State.startCheck(ServiceState.READY, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		StateSteps service2State = new StateSteps((Stateful) service2);
		service2State.startCheck(ServiceState.READY, 
				ServiceState.STARTING, ServiceState.STARTED);
		
		
		test.run();
		
		steps.checkNow();
		
		service1State.checkWait();
		service2State.checkWait();
		
		steps.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		test.stop();

		steps.checkNow();
		
		defaultServices.stop();
	}	
	
	public void testEmpty() throws InterruptedException {

		ParallelJob test = new ParallelJob();
		test.setExecutorService(new NowExecutor());
		
		StateSteps steps = new StateSteps(test);
		steps.startCheck(ParentState.READY, 
				ParentState.EXECUTING, 
				ParentState.READY);
		
		test.run();
		
		steps.checkWait();
		
		test.destroy();
		
		
	}
}
