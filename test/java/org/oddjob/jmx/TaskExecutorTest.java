package org.oddjob.jmx;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.tasks.BasicTask;
import org.oddjob.jobs.tasks.TaskException;
import org.oddjob.jobs.tasks.TaskExecutor;
import org.oddjob.jobs.tasks.TaskState;
import org.oddjob.jobs.tasks.TaskView;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class TaskExecutorTest extends TestCase {

	private static final Logger logger = 
			Logger.getLogger(TaskExecutorTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-------------------   " + getName() + "  ------------------------");
	}
	
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
		
		assertEquals(TaskState.COMPLETE, taskView.lastStateEvent().getState());
		
		assertEquals("Hello Jane", taskView.getTaskResponse());
		
		client.stop();
		
		server.destroy();
	}		

	
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
