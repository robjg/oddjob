/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.StateSteps;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.jobs.SequenceJob;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateConditions;

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
		
		int ran;
		
		private JobState state = JobState.COMPLETE;
		
		public void setState(JobState state) {
			this.state = state;
		}
				
		@Override			
		protected int execute() throws Throwable {
			ran++;
			
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
		test.setExecutorService(services.getPoolExecutor());
		
		StateSteps testState = new StateSteps(test);
		testState.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);
		
		test.run();
		
		testState.checkNow();
		
		testState.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		logger.info("Running dependant.");
		
		dependant.run();

		testState.checkWait();
				
		assertEquals(1, job.ran);
		
		testState.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		job.hardReset();
		
		testState.checkNow();

		testState.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE);
		
		// trigger won't fire because event is the same.
		test.run();

		assertEquals(1, job.ran);
		
		testState.checkNow();
		
		testState.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		if (new Date().equals(dependant.lastStateEvent().getTime())) {
			logger.info("Sleeping for a millisecond.");
			Thread.sleep(1);
		}
		
		logger.info("Running dependant again.");
		
		dependant.hardReset();
		dependant.run();
		
		testState.checkWait();
		
		assertEquals(2, job.ran);
		
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
		
		testState.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.ACTIVE);
		
		test.run();

		testState.checkNow();
		
		testState.startCheck(ParentState.ACTIVE, ParentState.EXCEPTION);
		
		depends.run();
						
		testState.checkWait();
		
		job.setState(JobState.INCOMPLETE);
		
		depends.hardReset();
		test.hardReset();
		
		testState.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.INCOMPLETE);
		
		test.run();
		
		Thread.sleep(1);
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
		
		testState.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.COMPLETE);
		
		test.run();
		depends.run();
						
		testState.checkWait();
		
		testState.startCheck(ParentState.COMPLETE, ParentState.READY, 
				ParentState.DESTROYED);
		
		test.setJob(null);
		
		job.destroy();
		
		test.destroy();
		
		depends.destroy();
		
		testState.checkNow();
		
		services.stop();
	}
	
	public void stopBeforeTriggered() throws FailedToStopException {
		
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
		
		test.run();
		
		assertEquals(ParentState.EXECUTING, test.lastStateEvent().getState());
		
		test.stop();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
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

		test.run();
		
		on.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		assertEquals(test, session.saved);
		
		Trigger copy = (Trigger) Helper.copy(test);		
		
		assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());
		
		copy.setExecutorService(new OurOddjobServices());
		copy.setOn(on);
		copy.setJob(sample);

		copy.hardReset();
		
		if (new Date().equals(copy.lastStateEvent().getTime())) {
			logger.info("Sleeping for a millisecond.");
			Thread.sleep(1);
		}
		
		copy.run();
		
		assertEquals(ParentState.ACTIVE, copy.lastStateEvent().getState());
		assertEquals(JobState.READY, sample.lastStateEvent().getState());
		
		on.hardReset();
		
		on.run();
		
		assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());
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
		
		test.run();
		
		assertEquals(null, sequence.getCurrent());
		
		on.run();
		
		assertEquals(new Integer(1), sequence.getCurrent());
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		test.hardReset();
		
		if (System.currentTimeMillis() == test.lastStateEvent().getTime().getTime()) {
			Thread.sleep(1);
		}
		
		test.run();
		
		assertEquals(ParentState.ACTIVE, test.lastStateEvent().getState());
		
		on.hardReset();
		on.run();
		
		assertEquals(new Integer(2), sequence.getCurrent());
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
	}

	public void testNoChild() throws Exception {
		
		FlagState on = new FlagState();
		on.setState(JobState.INCOMPLETE);
		
		Trigger test = new Trigger();
		
		test.setExecutorService(new OurOddjobServices());
		test.setOn(on);
		test.setState(StateConditions.INCOMPLETE);
		
		test.run();
		
		assertEquals(ParentState.ACTIVE, test.lastStateEvent().getState());
		
		on.run();
				
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		test.destroy();
	}
	
	public void testInOddjob() throws InterruptedException {
		
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
            "      <echo id='stop' text='Now you can stop.'/>" +
			"     </job>" +
			"    </si:trigger>" +
			"    <folder>" +
			"     <jobs>" +
            "      <echo text='Triggered!' id='thing1'/>" +
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
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());

		Runnable runnable = (Runnable) new OddjobLookup(oddjob).lookup("thing1");
		runnable.run();
		
		WaitJob wj = new WaitJob();
		wj.setFor(new OddjobLookup(oddjob).lookup("trigger"));
		wj.setState(StateConditions.COMPLETE);
		wj.run();

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
            "      <echo id='stop' text='Now you can stop.'/>" +
			"     </job>" +
			"    </si:trigger>" +
			"    <folder>" +
			"     <jobs>" +
            "      <echo text='Triggered!' id='thing1'/>" +
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
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());

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
	
	public void testExample() throws InterruptedException {
		

		DefaultExecutors services = new DefaultExecutors(); 
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/scheduling/TriggerExample.xml",
				getClass().getClassLoader()));
		oddjob.setOddjobExecutors(services);
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Runnable thing1 = (Runnable) lookup.lookup("thing1");
		thing1.run();
		Runnable thing2 = (Runnable) lookup.lookup("thing2");
		thing2.run();
		
		WaitJob wj = new WaitJob();
		wj.setFor(lookup.lookup("trigger"));
		wj.setState(StateConditions.COMPLETE);
		wj.run();

		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		services.stop();
		
		oddjob.destroy();
	}
}
