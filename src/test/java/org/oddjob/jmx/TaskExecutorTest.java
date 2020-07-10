package org.oddjob.jmx;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.tasks.*;
import org.oddjob.state.GenericState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class TaskExecutorTest extends OjTestCase {

	private static final Logger logger = 
			LoggerFactory.getLogger(TaskExecutorTest.class);
	
   @Before
   public void setUp() throws Exception {

		
		logger.info("-------------------   " + getName() + "  ------------------------");
	}
	
   @Test
	public void testTaskViewProxyPropergateStateAndTaskResonse() throws ArooaPropertyException, ArooaConversionException, TaskException, FailedToStopException {
		
		Oddjob server = new Oddjob();
		server.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/TaskExecutorServer.xml", 
				getClass().getClassLoader()));
		
		server.run();
		
		assertEquals(ParentState.STARTED, 
				server.lastStateEvent().getState());
		
		JMXClientJob client = new JMXClientJob();
		client.setArooaSession(new StandardArooaSession());
		client.run();
		
		OddjobLookup lookup = new OddjobLookup(client);
		
		TaskExecutor taskExecutor = lookup.lookup("task-executor",
				TaskExecutor.class);
		
		Properties properties = new Properties();
		properties.setProperty("name", "Jane");
		
		TaskView taskView = taskExecutor.execute(new BasicTask(properties));

	   assertThat(GenericState.statesEquivalent(TaskState.COMPLETE, taskView.lastStateEvent().getState()),
			   Matchers.is(true));
		
		assertEquals("Hello Jane", taskView.getTaskResponse());
		
		client.stop();
		
		server.destroy();
	}		

	
   @Test
	public void testClientExecutesTaskThatCompletesOK() {
		
		Oddjob server = new Oddjob();
		server.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/TaskExecutorServer.xml", 
				getClass().getClassLoader()));
		
		server.run();
		
		assertEquals(ParentState.STARTED, 
				server.lastStateEvent().getState());
		
		Oddjob client = new Oddjob();
		client.setConfiguration(new XMLConfiguration(
				"org/oddjob/jmx/TaskExecutorClient.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			client.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				client.lastStateEvent().getState());
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("Hello Jane", lines[0].trim());
		assertEquals("Hello Jane", lines[1].trim());
		assertEquals(2, lines.length);
		
		client.destroy();
		
		server.destroy();
	}
}
