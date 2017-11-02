package org.oddjob.sql;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.BufferType;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;

public class GrabbingWithSQLTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(GrabbingWithSQLTest.class);

    @Before
    public void setUp() throws Exception {
		logger.info("-----------------  " + getName() + "  --------------------");
	}
	
    @After
    public void tearDown() throws Exception {
		
		ConnectionType ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		BufferType buffer = new BufferType();
		buffer.setText("shutdown");
		buffer.configured();
		
		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
   @Test
	public void testSimpleExample() 
	throws ArooaPropertyException, ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/GrabbingWithSQLSimple.xml",
				getClass().getClassLoader()));		
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
				
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Stateful grabbers = lookup.lookup("grabbers", Stateful.class);
		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.COMPLETE);
		wait.setFor(grabbers);
		wait.run();
		
		Stateful echo1 = lookup.lookup("echo1", Stateful.class);
		Stateful echo2 = lookup.lookup("echo2", Stateful.class);
		
		State echo1State = echo1.lastStateEvent().getState();
		State echo2State = echo2.lastStateEvent().getState();
		
		assertTrue(echo1State == JobState.COMPLETE && 
				echo2State == JobState.READY
				|| 
				echo1State == JobState.READY && 
				echo2State == JobState.COMPLETE); 
		
		((Resetable) grabbers).hardReset();
		((Runnable) grabbers).run();
		
		wait.hardReset();
		wait.run();
		
		echo1State = echo1.lastStateEvent().getState();
		echo2State = echo2.lastStateEvent().getState();
		
		assertTrue(echo1State == JobState.READY && 
				echo2State == JobState.READY);
		
		Object sequenceJob = lookup.lookup("sequence");
		((Resetable) sequenceJob).hardReset();
		((Runnable) sequenceJob).run();
		
		((Resetable) grabbers).hardReset();
		((Runnable) grabbers).run();
		
		wait.hardReset();
		wait.run();
		
		echo1State = echo1.lastStateEvent().getState();
		echo2State = echo2.lastStateEvent().getState();
		
		assertTrue(echo1State == JobState.COMPLETE && 
				echo2State == JobState.READY
				|| 
				echo1State == JobState.READY && 
				echo2State == JobState.COMPLETE); 
		
		oddjob.destroy();
	}
	
   @Test
	public void testFailedWinner() 
	throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/GrabbingWithSQLFailure.xml",
				getClass().getClassLoader()));		
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);

		Stateful grabber1 = lookup.lookup("grabber1", Stateful.class);
		Stateful grabber2 = lookup.lookup("grabber2", Stateful.class);

		Stateful winner = null;

		while (true) {

			State grabber1State = grabber1.lastStateEvent().getState();
			State grabber2State = grabber2.lastStateEvent().getState();
			
			if (grabber1State == JobState.INCOMPLETE
					&& grabber2State == JobState.EXECUTING) {
				winner = grabber1;
				break;
			}

			
			if (grabber2State == JobState.INCOMPLETE 
					&& grabber1State == JobState.EXECUTING) {
				winner = grabber2;
				break;
			}
			
			logger.info("Sleeping, states are still " + grabber1State + 
					" and " + grabber2State);
			
			Thread.sleep(200);
		}
		
		DynaBean variables = lookup.lookup("vars", DynaBean.class);		
		variables.set("state", new ArooaObject("COMPLETE"));
		
		((Resetable) winner).softReset();
		((Runnable) winner).run();
		
		Stateful grabbers = lookup.lookup("grabbers", Stateful.class);
		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.COMPLETE);
		wait.setFor(grabbers);
		wait.run();
				
		oddjob.destroy();
	}
	
   @Test
	public void testStoppingGrabber() 
	throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/GrabbingWithSQLFailure.xml",
				getClass().getClassLoader()));		
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);

		Stateful grabber1 = lookup.lookup("grabber1", Stateful.class);
		Stateful grabber2 = lookup.lookup("grabber2", Stateful.class);

		Stateful looser = null;

		while (true) {

			State grabber1State = grabber1.lastStateEvent().getState();
			State grabber2State = grabber2.lastStateEvent().getState();
			
			int looserPollingCount = lookup.lookup("keeper-service.pollerCount", int.class);
			
			if (grabber1State == JobState.INCOMPLETE 
					&& looserPollingCount == 1) {
				looser = grabber2;
				break;
			}

			
			if (grabber2State == JobState.INCOMPLETE
					&& looserPollingCount == 1) {
				looser = grabber1;
				break;
			}
			
			logger.info("Sleeping, states are still " + grabber1State + 
					" and " + grabber2State);
			
			Thread.sleep(100);
		}
		
		assertEquals(JobState.EXECUTING, looser.lastStateEvent().getState());
		
		((Stoppable) looser).stop();
		
		Stateful grabbers = lookup.lookup("grabbers", Stateful.class);
		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.INCOMPLETE);
		wait.setFor(grabbers);
		wait.run();
				
		assertEquals(0, lookup.lookup("keeper-service.pollerCount"));
		
		assertEquals(JobState.INCOMPLETE, looser.lastStateEvent().getState());
		
		oddjob.stop();
		
		oddjob.destroy();
	}
	
   @Test
	public void testStoppingService() 
	throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/GrabbingWithSQLFailure.xml",
				getClass().getClassLoader()));		
		
		oddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);

		Stateful grabber1 = lookup.lookup("grabber1", Stateful.class);
		Stateful grabber2 = lookup.lookup("grabber2", Stateful.class);

		Stateful looser = null;

		while (true) {

			State grabber1State = grabber1.lastStateEvent().getState();
			State grabber2State = grabber2.lastStateEvent().getState();
			
			int looserPollingCount = lookup.lookup("keeper-service.pollerCount", int.class);
			
			if (grabber1State == JobState.INCOMPLETE &&
					looserPollingCount == 1) {
				looser = grabber2;
				break;
			}
			
			if (grabber2State == JobState.INCOMPLETE &&
					looserPollingCount == 1) {
				looser = grabber1;
				break;
			}
			
			logger.info("Sleeping, states are still " + grabber1State + 
					" and " + grabber2State);
			
			Thread.sleep(200);
		}
		
		assertEquals(JobState.EXECUTING, looser.lastStateEvent().getState());
		
		Stoppable keeperService = lookup.lookup("keeper-service", 
				Stoppable.class);
		
		keeperService.stop();
		
		Stateful grabbers = lookup.lookup("grabbers", Stateful.class);
		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.INCOMPLETE);
		wait.setFor(grabbers);
		wait.run();
				
		assertEquals(0, lookup.lookup("keeper-service.pollerCount"));
		
		assertEquals(JobState.INCOMPLETE, looser.lastStateEvent().getState());
		
		oddjob.stop();
		
		oddjob.destroy();
	}
}
