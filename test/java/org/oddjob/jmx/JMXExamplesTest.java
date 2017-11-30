package org.oddjob.jmx;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import java.io.File;
import java.util.Properties;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;
import org.oddjob.tools.FragmentHelper;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;

public class JMXExamplesTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(JMXExamplesTest.class);

	Oddjob serverOddjob;
	Oddjob clientOddjob;
	
   @Before
   public void setUp() throws Exception {

		
		logger.info("----------------  " + getName() + "  -----------------");
	}

   @After
   public void tearDown() throws Exception {

		
		if (clientOddjob != null) {
			clientOddjob.destroy();
		}
		if (serverOddjob != null) {
			serverOddjob.destroy();
		}
	}
	
   @Test
	public void testSimpleClientServerExample() throws ArooaParseException, FailedToStopException {
		
		Properties props = new Properties();
		props.setProperty("hosts.freds.pc", "localhost");
		
		OurDirs dirs = new OurDirs();
		
		File testDir = dirs.relative("test/java/org/oddjob/jmx");
		
		serverOddjob = new Oddjob();
		serverOddjob.setFile(new File(testDir, "ServerExample.xml"));

		serverOddjob.run();
		
		assertEquals(ParentState.STARTED, 
				serverOddjob.lastStateEvent().getState());
		
		FragmentHelper helper = new FragmentHelper();
		helper.setProperties(props);
		
		JMXClientJob client = (JMXClientJob) helper.createComponentFromResource(
				"org/oddjob/jmx/ClientExample.xml");

		StateSteps clientSteps = new StateSteps(client);
		clientSteps.startCheck(ServiceState.STARTABLE, ServiceState.STARTING, 
				ServiceState.STARTED);
		
		client.run();
		
		clientSteps.checkNow();
		
		clientSteps.startCheck(ServiceState.STARTED, ServiceState.STOPPED);
		
		client.stop();
		
		clientSteps.checkNow();
	}
	
   @Test
	public void testClientRunsServerJobExample() throws InterruptedException, ArooaPropertyException, ArooaConversionException {
		
		Properties props = new Properties();
		props.setProperty("hosts.freds.pc", "localhost");
		
		OurDirs dirs = new OurDirs();
		
		File testDir = dirs.relative("test/java/org/oddjob/jmx");
		
		serverOddjob = new Oddjob();
		serverOddjob.setProperties(props);
		serverOddjob.setFile(new File(testDir, "ServerExample.xml"));

		serverOddjob.run();
		
		assertEquals(ParentState.STARTED, 
				serverOddjob.lastStateEvent().getState());
		
		OddjobLookup serverLookup = new OddjobLookup(serverOddjob);
		
		Stateful serverJob = serverLookup.lookup("server-jobs/greeting", 
				Stateful.class);
	
		clientOddjob = new Oddjob();
		clientOddjob.setProperties(props);
		clientOddjob.setFile(new File(testDir, "ClientRunsServerJob.xml"));

		StateSteps serverJobStates = new StateSteps(serverJob);
		serverJobStates.startCheck(JobState.READY, JobState.EXECUTING, 
				JobState.COMPLETE);
		
		StateSteps clientOddjobStates = new StateSteps(clientOddjob);
		clientOddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);

		logger.info("** running Oddjob Client **");
		clientOddjob.run();
		
		clientOddjobStates.checkWait();
		
		serverJobStates.checkWait();
	}
	
   @Test
	public void testClientTriggersOnServerJobExample() throws InterruptedException, ArooaPropertyException, ArooaConversionException {
		
		Properties props = new Properties();
		props.setProperty("hosts.freds.pc", "localhost");
		
		OurDirs dirs = new OurDirs();
		
		File testDir = dirs.relative("test/java/org/oddjob/jmx");
		
		serverOddjob = new Oddjob();
		serverOddjob.setProperties(props);
		serverOddjob.setFile(new File(testDir, "ServerExample.xml"));

		serverOddjob.run();
		
		assertEquals(ParentState.STARTED, 
				serverOddjob.lastStateEvent().getState());
		
		OddjobLookup serverLookup = new OddjobLookup(serverOddjob);
		
		Runnable serverJob = serverLookup.lookup("server-jobs/greeting", 
				Runnable.class);
	
		clientOddjob = new Oddjob();
		clientOddjob.setProperties(props);
		clientOddjob.setFile(new File(testDir, "ClientTrigger.xml"));

		clientOddjob.run();
		
		OddjobLookup clientLookup = new OddjobLookup(clientOddjob);
		
		Stateful localJob = clientLookup.lookup("local-job", 
				Stateful.class);
		
		assertEquals(ParentState.STARTED, 
				clientOddjob.lastStateEvent().getState());
		
		assertEquals(JobState.READY, 
				localJob.lastStateEvent().getState());
		
		StateSteps state = new StateSteps(clientOddjob);
		
		state.startCheck(ParentState.STARTED, ParentState.ACTIVE, 
				ParentState.COMPLETE);
		
		serverJob.run();
		
		state.checkWait();

		// Todo Fix this
//		clientOddjob.destroy();
//		serverOddjob.destroy();
	}

}
