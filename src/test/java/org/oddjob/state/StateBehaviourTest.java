package org.oddjob.state;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateBehaviourTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(StateBehaviourTest.class);
	
    @Before
    public void setUp() throws Exception {
		
		logger.info("--------------------  " + getName() + "  ----------------");
	}
	
	/**
	 * Check a cut doesn't set an empty job to complete. 
	 * This is why an empty structural job is READY.
	 * 
	 * @throws ArooaParseException
	 */
   @Test
	public void testEmptyParentState() throws ArooaParseException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/state/StateBehaviourEmptyParentTest.xml",
				getClass().getClassLoader()));
			
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object sequential = lookup.lookup("sequential");
		Object echo = lookup.lookup("echo");
		
		StateSteps checker = new StateSteps((Stateful) sequential);
		checker.startCheck(ParentState.READY);
		
		DragPoint dp = oddjob.provideConfigurationSession().dragPointFor(echo);
		
		DragTransaction t = dp.beginChange(ChangeHow.FRESH);
		dp.delete();
		t.commit();
		
		checker.checkNow();
		
		oddjob.destroy();
	}
	
   @Test
	public void testEmptySequential() throws ArooaParseException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/state/StateBehaviourEmptySequentialTest.xml",
				getClass().getClassLoader()));
			
		oddjob.run();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object echo = lookup.lookup("echo");
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) echo).lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
   @Test
	public void testExecutingSequential() throws ArooaParseException, InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/state/StateBehaviourExecutingSequentialTest.xml",
				getClass().getClassLoader()));
			
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object wait = lookup.lookup("wait");
		Object sequential = lookup.lookup("sequential");
		Object echo = lookup.lookup("echo");
		
		
		StateSteps checker = new StateSteps((Stateful) wait);
		checker.startCheck(JobState.READY, 
				JobState.EXECUTING);
		
		new Thread((Runnable) sequential).start();
		
		checker.checkWait();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) echo).lastStateEvent().getState());
		
		((Stoppable) sequential).stop();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public static class OurService {
		public void start() {}
		public void stop() {}
	}
	
   @Test
	public void testActiveSequential() throws ArooaParseException, InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/state/StateBehaviourActiveSequentialTest.xml",
				getClass().getClassLoader()));
			
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object sequential = lookup.lookup("sequential");
		Object service = lookup.lookup("service");
		Object echo = lookup.lookup("echo");
		
		
		StateSteps sequentialStates = new StateSteps((Stateful) sequential);
		sequentialStates.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		((Runnable) sequential).run();
		
		sequentialStates.checkNow();
		
		assertEquals(ServiceState.STARTED, 
				((Stateful) service).lastStateEvent().getState());
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

		oddjob.run();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) echo).lastStateEvent().getState());
		
		((Stoppable) sequential).stop();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}

   @Test
	public void testServiceActiveSequential() throws ArooaParseException, InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/state/StateBehaviourServiceSequentialTest.xml",
				getClass().getClassLoader()));
			
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object sequential = lookup.lookup("sequential");
		Object service = lookup.lookup("service");
		Object echo = lookup.lookup("echo");
		
		
		((Runnable) service).run();
		
		assertEquals(ServiceState.STARTED, 
				((Stateful) service).lastStateEvent().getState());
		
		StateSteps checker = new StateSteps((Stateful) sequential);
		checker.startCheck(ParentState.READY, 
				ParentState.EXECUTING, ParentState.STARTED);
		
		((Runnable) sequential).run();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

		oddjob.run();
		
		checker.checkNow();
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
		
		assertEquals(JobState.COMPLETE, 
				((Stateful) echo).lastStateEvent().getState());
		
		((Stoppable) sequential).stop();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	
   @Test
	public void testRunningChildren() throws ArooaParseException, InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/state/StateBehaviourRunningChildrenTest.xml",
				getClass().getClassLoader()));
			
		oddjob.load();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object sequential = lookup.lookup("sequential");
		Object wait1 = lookup.lookup("wait1");
		Object wait2 = lookup.lookup("wait2");
		Object echo = lookup.lookup("echo");
		
		
		StateSteps checker = new StateSteps((Stateful) wait2);
		checker.startCheck(JobState.READY, 
				JobState.EXECUTING);
		
		new Thread((Runnable) wait2).start();
		
		checker.checkWait();
		
		StateSteps checker2 = new StateSteps((Stateful) wait1);
		checker2.startCheck(JobState.READY, 
				JobState.EXECUTING);
		
		new Thread((Runnable) sequential).start();
		
		checker2.checkWait();
		
		assertEquals(ParentState.EXECUTING, 
				((Stateful) sequential).lastStateEvent().getState());
		
		assertEquals(JobState.READY, 
				((Stateful) echo).lastStateEvent().getState());

		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());
		
		assertEquals(JobState.READY, 
				((Stateful) echo).lastStateEvent().getState());
		
		((Stoppable) sequential).stop();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		// start a wait again and check stopped
		
		checker.startCheck(JobState.COMPLETE, JobState.READY, 
				JobState.EXECUTING);

		((Resettable) wait2).hardReset();
		
		new Thread((Runnable) wait2).start();
		
		checker.checkWait();

		assertEquals(ParentState.ACTIVE, oddjob.lastStateEvent().getState());
		
		((Stoppable) sequential).stop();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
}
