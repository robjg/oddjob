package org.oddjob.sql;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Helper;
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
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;
import org.oddjob.state.ParentState;

public class GrabbingWithSQLTest extends TestCase {
	private static final Logger logger = Logger.getLogger(GrabbingWithSQLTest.class);

	@Override
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
		
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
		
		assertTrue(Helper.getJobState(echo1) == JobState.COMPLETE && 
				Helper.getJobState(echo2) == JobState.READY
				|| 
				Helper.getJobState(echo1) == JobState.READY && 
				Helper.getJobState(echo2) == JobState.COMPLETE); 
		
		((Resetable) grabbers).hardReset();
		((Runnable) grabbers).run();
		
		wait.hardReset();
		wait.run();
		
		assertTrue(Helper.getJobState(echo1) == JobState.READY && 
				Helper.getJobState(echo2) == JobState.READY);
		
		Object sequenceJob = lookup.lookup("sequence");
		((Resetable) sequenceJob).hardReset();
		((Runnable) sequenceJob).run();
		
		((Resetable) grabbers).hardReset();
		((Runnable) grabbers).run();
		
		wait.hardReset();
		wait.run();
		
		assertTrue(Helper.getJobState(echo1) == JobState.COMPLETE && 
				Helper.getJobState(echo2) == JobState.READY
				|| 
				Helper.getJobState(echo1) == JobState.READY && 
				Helper.getJobState(echo2) == JobState.COMPLETE); 
		
		oddjob.destroy();
	}
	
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

		Object winner = null;
		Object looser = null;

		while (true) {

			State grabber1State = Helper.getJobState(grabber1);
			State grabber2State = Helper.getJobState(grabber2);
			
			if (grabber1State == JobState.INCOMPLETE) {
				winner = grabber1;
				looser = grabber2;
				break;
			}

			
			if (grabber2State == JobState.INCOMPLETE) {
				winner = grabber2;
				looser = grabber1;
				break;
			}
			
			logger.info("Sleeping, state " + grabber1State + 
					" and " + grabber2State);
			
			Thread.sleep(1000);
		}
		
		assertEquals(JobState.EXECUTING, Helper.getJobState(looser));
		
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

		Object looser = null;

		while (true) {

			State grabber1State = Helper.getJobState(grabber1);
			State grabber2State = Helper.getJobState(grabber2);
			
			if (grabber1State == JobState.INCOMPLETE) {
				looser = grabber2;
				break;
			}

			
			if (grabber2State == JobState.INCOMPLETE) {
				looser = grabber1;
				break;
			}
			
			logger.info("Sleeping, state " + grabber1State + 
					" and " + grabber2State);
			
			Thread.sleep(1000);
		}
		
		assertEquals(JobState.EXECUTING, Helper.getJobState(looser));
		
		assertEquals(1, lookup.lookup("keeper-service.pollerCount"));
		
		((Stoppable) looser).stop();
		
		Stateful grabbers = lookup.lookup("grabbers", Stateful.class);
		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.INCOMPLETE);
		wait.setFor(grabbers);
		wait.run();
				
		assertEquals(0, lookup.lookup("keeper-service.pollerCount"));
		
		assertEquals(JobState.INCOMPLETE, Helper.getJobState(looser));
		
		oddjob.stop();
		
		oddjob.destroy();
	}
	
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

		Object looser = null;

		while (true) {

			State grabber1State = Helper.getJobState(grabber1);
			State grabber2State = Helper.getJobState(grabber2);
			
			if (grabber1State == JobState.INCOMPLETE) {
				looser = grabber2;
				break;
			}

			
			if (grabber2State == JobState.INCOMPLETE) {
				looser = grabber1;
				break;
			}
			
			logger.info("Sleeping, state " + grabber1State + 
					" and " + grabber2State);
			
			Thread.sleep(1000);
		}
		
		assertEquals(JobState.EXECUTING, Helper.getJobState(looser));
		
		assertEquals(1, lookup.lookup("keeper-service.pollerCount"));
		
		Stoppable keeperService = lookup.lookup("keeper-service", 
				Stoppable.class);
		
		keeperService.stop();
		
		Stateful grabbers = lookup.lookup("grabbers", Stateful.class);
		
		WaitJob wait = new WaitJob();
		wait.setState(StateConditions.INCOMPLETE);
		wait.setFor(grabbers);
		wait.run();
				
		assertEquals(0, lookup.lookup("keeper-service.pollerCount"));
		
		assertEquals(JobState.INCOMPLETE, Helper.getJobState(looser));
		
		oddjob.stop();
		
		oddjob.destroy();
	}
}
