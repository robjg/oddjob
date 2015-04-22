/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.SequenceJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateListener;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;

/**
 * 
 */
public class TriggerTest extends TestCase {

	private static final Logger logger = 
		Logger.getLogger(TriggerTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("----------------- " + getName() + " -------------");
	}
		
	private class OurDependant extends SimpleJob {
		private StateListener listenerCheck;

		private JobState state;
		
		public OurDependant() {
			this(JobState.COMPLETE);
		}
		
		public OurDependant(JobState state) {
			this.state = state;
		}
		
//		public void setState(JobState state) {
//			this.state = state;
//		}
		
		@Override			
		protected int execute() throws Throwable {
			switch (state) {
			case COMPLETE: 
				return 0;
			case INCOMPLETE:
				return 1;
			default:
				throw new RuntimeException("Deliberate Exception.");
			}		
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			assertNull(listenerCheck);
			assertNotNull(listener);
			this.listenerCheck = listener;
			super.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			assertNotNull(listenerCheck);
			assertEquals(listenerCheck, listener);
			listenerCheck = null;
			super.removeStateListener(listener);
		}
	}
	
	private class OurJob extends SimpleJob {
		private StateListener listenerCheck;
		
		AtomicInteger ran = new AtomicInteger();
		
		private JobState state = JobState.COMPLETE;
		
		public void setState(JobState state) {
			this.state = state;
		}
				
		@Override			
		protected int execute() throws Throwable {
			ran.incrementAndGet();
			
			switch (state) {
			case COMPLETE: 
				return 0;
			case INCOMPLETE:
				return 1;
			default:
				throw new RuntimeException("Deliberate Exception.");
			}		
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			assertNull(listenerCheck);
			assertNotNull(listener);
			this.listenerCheck = listener;
			super.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			assertNotNull(listenerCheck);
			assertEquals(listenerCheck, listener);
			listenerCheck = null;
			super.removeStateListener(listener);
		}
	}
	
	public void testSimpleTrigger() throws Exception {
		
		DefaultExecutors services = new DefaultExecutors();
		
		OurJob job = new OurJob();
		
		OurDependant dependant = new OurDependant();
		
		Trigger test = new Trigger();
		test.setOn(dependant);
		test.setJob(job);
		test.setNewOnly(true);
		test.setExecutorService(services.getPoolExecutor());
		
		StateSteps testState = new StateSteps(test);
		testState.startCheck(TimerState.STARTABLE, TimerState.STARTING, TimerState.STARTED);

		test.run();
		
		testState.checkNow();
		
		testState.startCheck(TimerState.STARTED, TimerState.COMPLETE);
		
		logger.info("** Running dependant.");
		
		dependant.run();

		testState.checkWait();
				
		assertEquals(1, job.ran.get());
		
		testState.startCheck(TimerState.COMPLETE, TimerState.STARTABLE);
		
		job.hardReset();
		
		testState.checkNow();

		testState.startCheck(TimerState.STARTABLE, TimerState.STARTING,
				TimerState.STARTED);
		
		logger.info("** Running trigger again.");
		
		// trigger won't fire because event is the same.
		test.run();

		testState.checkNow();
		
		assertEquals(1, job.ran.get());
		
		testState.startCheck(TimerState.STARTED, TimerState.COMPLETE);
		
		while (new Date().equals(dependant.lastStateEvent().getTime())) {
			// Note that sleeping for 1 millisecond is operating system
			// dependent and there is no guarantee that it is a millisecond 
			// later after this operation - hence the while.
			Thread.sleep(1);
			logger.info("Slept until [" + 
					DateHelper.formatDateTime(new Date()) + "]");
		}
		
		logger.info("** Running dependant again.");
		
		dependant.hardReset();
		dependant.run();
		
		testState.checkWait();
		
		assertEquals(2, job.ran.get());
		
		logger.info("Shutting down.");
		
		services.stop();
	}
	
	public void testTriggerReflectsChildJobState() throws Exception {
		
		final DefaultExecutors services = new DefaultExecutors();
		
		OurJob job = new OurJob();
		job.setState(JobState.EXCEPTION);
		
		OurDependant depends = new OurDependant();
		
		Trigger test = new Trigger();
		test.setOn(depends);
		test.setJob(job);
		test.setExecutorService(services.getPoolExecutor());
		
		StateSteps testState = new StateSteps(test);
		
		testState.startCheck(TimerState.STARTABLE, TimerState.STARTING,
				TimerState.STARTED);
		
		test.run();

		testState.checkNow();
		
		testState.startCheck(TimerState.STARTED, TimerState.EXCEPTION);
		
		depends.run();
						
		testState.checkWait();
		
		job.setState(JobState.INCOMPLETE);
		
		depends.hardReset();
		test.hardReset();
		
		testState.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED, TimerState.INCOMPLETE);
		
		test.run();
		
		depends.run();
		
		testState.checkWait();
		
		services.stop();
	}
	
	public void testDestroyCycle() throws Exception {
		
		final DefaultExecutors services = new DefaultExecutors();
		
		OurJob job = new OurJob();
		
		OurDependant depends = new OurDependant();
		
		Trigger test = new Trigger();
		test.setOn(depends);
		test.setJob(job);
		test.setExecutorService(services.getPoolExecutor());
		
		StateSteps testState = new StateSteps(test);
		
		testState.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED, TimerState.COMPLETE);
		
		test.run();
		depends.run();
						
		testState.checkWait();
		
		testState.startCheck(TimerState.COMPLETE, TimerState.STARTABLE, 
				TimerState.DESTROYED);
		
		test.setJob(null);
		
		job.destroy();
		
		test.destroy();
		
		depends.destroy();
		
		testState.checkNow();
		
		services.stop();
	}
	
	public void testStopBeforeTriggered() throws FailedToStopException {
		
		class NeverRun extends SimpleJob {
			@Override
			protected int execute() throws Throwable {
				throw new Exception("Shouldn't Run.");
			}
		}
		
		Trigger test = new Trigger();
		test.setOn(new NeverRun());
		test.setJob(new NeverRun());
		test.setExecutorService(new MockExecutorService());
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED);
		
		test.run();
		
		testStates.checkNow();
				
		testStates.startCheck(TimerState.STARTED, TimerState.STARTABLE);
		
		test.stop();
		
		testStates.checkNow();
	}
	
	private class OurOddjobServices extends MockScheduledExecutorService {
		
		public Future<?> submit(Runnable runnable) {
			runnable.run();

			return new MockScheduledFuture<Void>();
		}
	};
	
	private class SerializeSession extends MockArooaSession {
		
		Object saved;
		
		@Override
		public ComponentPool getComponentPool() {
			return new MockComponentPool() {
				@Override
				public void configure(Object component) {
				}
				@Override
				public void save(Object component) {
					saved = component;
				}
			};
		}
	}
	
	
	public void testSerialize() throws Exception {
		
		FlagState sample = new FlagState();
		sample.setState(JobState.COMPLETE);
		
		FlagState on = new FlagState();
		on.setState(JobState.COMPLETE);
		
		SerializeSession session = new SerializeSession();
		
		Trigger test = new Trigger();
		
		test.setArooaSession(session);
		test.setExecutorService(new OurOddjobServices());
		test.setOn(on);
		test.setJob(sample);
		test.setNewOnly(true);

		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED, TimerState.COMPLETE);
		
		test.run();
		
		on.run();
		
		testStates.checkWait();
		
		assertEquals(test, session.saved);
		
		Trigger copy = (Trigger) OddjobTestHelper.copy(test);		
		
		assertEquals(TimerState.COMPLETE, copy.lastStateEvent().getState());
		
		copy.setExecutorService(new OurOddjobServices());
		copy.setOn(on);
		copy.setJob(sample);

		copy.hardReset();
		
		while (new Date().equals(copy.lastStateEvent().getTime())) {
			Thread.sleep(1);
			logger.info("Slept until [" + 
					DateHelper.formatDateTime(new Date()) + "]");
		}
		
		StateSteps copyStates = new StateSteps(copy);
		copyStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED);
		
		copy.run();

		copyStates.checkNow();
		assertEquals(JobState.READY, sample.lastStateEvent().getState());
		
		copyStates.startCheck(TimerState.STARTED, TimerState.COMPLETE);
		
		on.hardReset();
		
		on.run();
		
		copyStates.checkWait();
		
		assertEquals(JobState.COMPLETE, sample.lastStateEvent().getState());
	}
 
	public void testReset() throws Exception {
		
		SequenceJob sequence = new SequenceJob();
		sequence.setFrom(1);
		
		FlagState on = new FlagState();
		on.setState(JobState.INCOMPLETE);
		
		Trigger test = new Trigger();
		
		test.setExecutorService(new OurOddjobServices());
		test.setOn(on);
		test.setJob(sequence);
		test.setState(StateConditions.INCOMPLETE);
		test.setNewOnly(true);
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED);
		
		test.run();
		
		testStates.checkNow();
		testStates.startCheck(
				TimerState.STARTED, TimerState.COMPLETE);
		
		assertEquals(null, sequence.getCurrent());
		
		on.run();
		
		testStates.checkWait();
		
		assertEquals(new Integer(1), sequence.getCurrent());
		
		assertEquals(TimerState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		while (System.currentTimeMillis() == test.lastStateEvent().getTime().getTime()) {
			Thread.sleep(1);
			logger.info("Slept until [" + 
					DateHelper.formatDateTime(new Date()) + "]");
		}
		
		test.run();
		
		assertEquals(TimerState.STARTED, test.lastStateEvent().getState());
		
		on.hardReset();
		
		assertEquals(JobState.READY, on.lastStateEvent().getState());
		
		testStates.startCheck(
				TimerState.STARTED, TimerState.COMPLETE);
		
		on.run();
		
		testStates.checkWait();
		
		assertEquals(new Integer(2), sequence.getCurrent());
		
		assertEquals(TimerState.COMPLETE, test.lastStateEvent().getState());
	}

	public void testNoChild() throws Exception {
		
		FlagState on = new FlagState();
		on.setState(JobState.INCOMPLETE);
		
		Trigger test = new Trigger();
		
		test.setExecutorService(new OurOddjobServices());
		test.setOn(on);
		test.setState(StateConditions.INCOMPLETE);
		
		test.run();
		
		assertEquals(TimerState.STARTED, test.lastStateEvent().getState());
		
		on.run();
				
		assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());
		
		test.destroy();
	}
	
	public void testInOddjob() throws InterruptedException, ArooaPropertyException, ArooaConversionException {
		
		String xml = 
			"<oddjob xmlns:si='http://rgordon.co.uk/oddjob/scheduling'" +
			"        id='this'>" +
			" <job>" +
			"  <sequential>" +
			"	<jobs>" +
//			"    <rmireg/>" +
//			"    <server url='service:jmx:rmi://ignored/jndi/rmi://localhost/my-oddjob'" + 
//			"               root='${this}'/>" +
			"    <si:trigger on='${thing1}' id='trigger'>" +
			"     <job>" +
            "      <echo id='stop'>Now you can stop.</echo>" +
			"     </job>" +
			"    </si:trigger>" +
			"    <folder>" +
			"     <jobs>" +
            "      <echo id='thing1'>Triggered!</echo>" +
            "     </jobs>" +
            "    </folder>" +
            "   </jobs>" +
            "  </sequential>" +
            " </job>" +
            "</oddjob>";

		DefaultExecutors services = new DefaultExecutors(); 
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.setOddjobExecutors(services);
		
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Runnable runnable = lookup.lookup("thing1", Runnable.class);
		
		Stateful trigger = lookup.lookup("trigger", Stateful.class);

		StateSteps triggerStates = new StateSteps(trigger);
		triggerStates.startCheck(TimerState.STARTABLE, TimerState.STARTING, 
				TimerState.STARTED, TimerState.COMPLETE);
		
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

		runnable.run();
		
		triggerStates.checkWait();

		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		services.stop();
		
		oddjob.destroy();
	}

	public void testCuttingTriggerJob() throws InterruptedException, ArooaParseException {
		
		String xml = 
			"<oddjob xmlns:si='http://rgordon.co.uk/oddjob/scheduling'" +
			"        id='this'>" +
			" <job>" +
			"  <sequential>" +
			"	<jobs>" +
			"    <si:trigger on='${thing1}' id='trigger'>" +
			"     <job>" +
            "      <echo id='stop'>Now you can stop</echo>" +
			"     </job>" +
			"    </si:trigger>" +
			"    <folder>" +
			"     <jobs>" +
            "      <echo id='thing1'>Triggered!</echo>" +
            "     </jobs>" +
            "    </folder>" +
            "   </jobs>" +
            "  </sequential>" +
            " </job>" +
            "</oddjob>";

		DefaultExecutors services = new DefaultExecutors(); 
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.setOddjobExecutors(services);
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

		Object on = new OddjobLookup(oddjob).lookup("thing1");
		
		DragPoint drag = oddjob.provideConfigurationSession().dragPointFor(on);
		DragTransaction trn = drag.beginChange(ChangeHow.FRESH);
		drag.cut();
		try {
			trn.commit();
		} catch (ArooaParseException e) {
			trn.rollback();
			throw e;
		}
		
		assertEquals(ParentState.EXCEPTION, oddjob.lastStateEvent().getState());
		
		services.stop();
		
		oddjob.destroy();
	}
	
	public void testSimpleExample() throws InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/TriggerSimple.xml",
				getClass().getClassLoader()));
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

		StateSteps states = new StateSteps(oddjob);
		states.startCheck(ParentState.STARTED, ParentState.COMPLETE);
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Runnable important = (Runnable) lookup.lookup("important");
		important.run();
		
		states.checkWait();
		
		oddjob.destroy();
	}
	
	public void testExample() throws InterruptedException {
		

		DefaultExecutors services = new DefaultExecutors(); 
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/TriggerExample.xml",
				getClass().getClassLoader()));
		oddjob.setOddjobExecutors(services);
				
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful test = (Stateful) lookup.lookup("trigger");
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(TimerState.STARTABLE, 
				TimerState.STARTING, TimerState.STARTED);
		
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

		testStates.checkNow();
		testStates.startCheck(TimerState.STARTED, TimerState.COMPLETE);
		
		Runnable thing1 = (Runnable) lookup.lookup("thing1");
		thing1.run();
		Runnable thing2 = (Runnable) lookup.lookup("thing2");
		thing2.run();
		
		testStates.checkWait();

		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		services.stop();
		
		oddjob.destroy();
	}
	
	public void testCancelExample() throws InterruptedException {

		DefaultExecutors services = new DefaultExecutors(); 
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/TriggerCancelExample.xml",
				getClass().getClassLoader()));
		oddjob.setOddjobExecutors(services);
				
		oddjob.load();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful test = (Stateful) lookup.lookup("trigger");
		
		StateSteps testStates = new StateSteps(test);
		testStates.startCheck(TimerState.STARTABLE, 
				TimerState.STARTING, TimerState.STARTED);
		
		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

		testStates.checkNow();
		testStates.startCheck(TimerState.STARTED, TimerState.COMPLETE);
		
		Runnable ourJob = (Runnable) lookup.lookup("our-job");
		ourJob.run();
				
		testStates.checkWait();

		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		Stateful triggeredJob = (Stateful) lookup.lookup("triggered-job");

		assertEquals(JobState.READY, 
				triggeredJob.lastStateEvent().getState());
		services.stop();
		
		oddjob.destroy();
	}
	
	public void testStop() throws InterruptedException, FailedToStopException {
	
		DefaultExecutors services = new DefaultExecutors(); 
		
		Trigger test = new Trigger();
		test.setExecutorService(services.getPoolExecutor());
		
		WaitJob wait = new WaitJob();

		FlagState on = new FlagState();
		
		test.setOn(on);
		test.setJob(wait);
		
		StateSteps waitState = new StateSteps(wait);
		waitState.startCheck(JobState.READY, JobState.EXECUTING);
		
		test.run();
		
		on.run();
		
		waitState.checkWait();
		
		StateSteps testState = new StateSteps(test);
		testState.startCheck(TimerState.STARTED, TimerState.COMPLETE);

		test.stop();
		
		testState.checkWait();
		
		services.stop();
	}
}
