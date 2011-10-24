package org.oddjob.jmx;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FragmentHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.StateSteps;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ServiceState;

public class JMXExamplesTest extends TestCase {

	private static final Logger logger = Logger.getLogger(JMXExamplesTest.class);

	Oddjob serverOddjob;
	Oddjob clientOddjob;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("----------------  " + getName() + "  -----------------");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		if (clientOddjob != null) {
			clientOddjob.destroy();
		}
		if (serverOddjob != null) {
			serverOddjob.destroy();
		}
	}
	
	public void testSimpleClientServerExample() throws ArooaParseException {
		
		Properties props = new Properties();
		props.setProperty("hosts.freds.pc", "localhost");
		
		OurDirs dirs = new OurDirs();
		
		File testDir = dirs.relative("test/java/org/oddjob/jmx");
		
		serverOddjob = new Oddjob();
		serverOddjob.setFile(new File(testDir, "ServerExample.xml"));

		serverOddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				serverOddjob.lastStateEvent().getState());
		
		FragmentHelper helper = new FragmentHelper();
		helper.setProperties(props);
		
		JMXClientJob client = (JMXClientJob) helper.createComponentFromResource(
				"org/oddjob/jmx/ClientExample.xml");

		StateSteps clientSteps = new StateSteps(client);
		clientSteps.startCheck(ServiceState.READY, ServiceState.STARTING, 
				ServiceState.STARTED);
		
		client.run();
		
		clientSteps.checkNow();
		
		client.stop();
	}
	
	public void testClientRunsServerJobExample() throws InterruptedException, ArooaPropertyException, ArooaConversionException {
		
		Properties props = new Properties();
		props.setProperty("hosts.freds.pc", "localhost");
		
		OurDirs dirs = new OurDirs();
		
		File testDir = dirs.relative("test/java/org/oddjob/jmx");
		
		serverOddjob = new Oddjob();
		serverOddjob.setProperties(props);
		serverOddjob.setFile(new File(testDir, "ServerExample.xml"));

		serverOddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
				serverOddjob.lastStateEvent().getState());
		
		OddjobLookup serverLookup = new OddjobLookup(serverOddjob);
		
		Stateful serverJob = serverLookup.lookup("server-jobs/greeting", 
				Stateful.class);
	
		clientOddjob = new Oddjob();
		clientOddjob.setProperties(props);
		clientOddjob.setFile(new File(testDir, "ClientRunsServerJob.xml"));

		StateSteps steps = new StateSteps(serverJob);
		steps.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);
		
		clientOddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				clientOddjob.lastStateEvent().getState());
		
		steps.checkWait();
		
	}
	
	public void testClientTriggersOnServerJobExample() throws InterruptedException, ArooaPropertyException, ArooaConversionException {
		
		Properties props = new Properties();
		props.setProperty("hosts.freds.pc", "localhost");
		
		OurDirs dirs = new OurDirs();
		
		File testDir = dirs.relative("test/java/org/oddjob/jmx");
		
		serverOddjob = new Oddjob();
		serverOddjob.setProperties(props);
		serverOddjob.setFile(new File(testDir, "ServerExample.xml"));

		serverOddjob.run();
		
		assertEquals(ParentState.ACTIVE, 
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
		
		assertEquals(ParentState.ACTIVE, 
				clientOddjob.lastStateEvent().getState());
		
		assertEquals(JobState.READY, 
				localJob.lastStateEvent().getState());
		
		StateSteps state = new StateSteps(clientOddjob);
		
		state.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
		
		serverJob.run();
		
		state.checkWait();
		
	}

}
