package org.oddjob.jobs.tasks;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.util.Properties;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.input.InputRequest;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.jobs.tasks.TaskExecutionService;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class TaskExecutionServiceTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(TaskExecutionServiceTest.class);
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("--------------------  " + getName() + "  ------------------");
	}
	
   @Test
	public void testTaskResponseIsSetCorrectly() throws ArooaPropertyException, ArooaConversionException, TaskException {
		
		File file = new File(getClass().getResource(
				"TaskExecutorServiceResponse.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		TaskExecutionService test = lookup.lookup("hello-service", 
				TaskExecutionService.class);
		
		Properties properties = new Properties();
		
		properties.setProperty("some.name", "Jane");
		
		TaskView taskView = test.execute(new BasicTask(properties));
		
		assertEquals(TaskState.COMPLETE, taskView.lastStateEvent().getState());
		
		assertEquals("Hello Jane.", taskView.getTaskResponse());
	}
	
   @Test
	public void testSimpleRequestAndExecuteExample() throws ArooaPropertyException, ArooaConversionException, TaskException {
		
		File file = new File(getClass().getResource(
				"ParameterisedExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		TaskExecutionService test = lookup.lookup("parameterised-job", 
				TaskExecutionService.class);
		
		InputRequest[] requests = test.getParameterInfo();
		
		assertEquals(4, requests.length);
		
		Properties properties = new Properties();
		
		properties.setProperty("favourite.fruit", "banana");
		properties.setProperty("favourite.colour", "blue");
		
		TaskView taskView = test.execute(new BasicTask(properties));
		
		assertEquals(TaskState.COMPLETE, 
				taskView.lastStateEvent().getState());
		assertEquals("OK", taskView.getTaskResponse());
		
		String text = lookup.lookup("echo.text", String.class);
		
		assertEquals("Favourite Fruit: banana, Favourite Colour: blue", 
				text);
		
		properties.setProperty("favourite.fruit", "kiwi");
		properties.setProperty("favourite.colour", "pink");
		
		taskView = test.execute(new BasicTask(properties));
		
		assertEquals(TaskState.COMPLETE, 
				taskView.lastStateEvent().getState());
		assertEquals("OK", taskView.getTaskResponse());
		
		text = lookup.lookup("echo.text", String.class);
		
		assertEquals("Favourite Fruit: kiwi, Favourite Colour: pink", 
				text);
		
		oddjob.destroy();
		
	}
	
   @Test
	public void testPropertiesAreSegreatedBetweenServices() throws ArooaPropertyException, ArooaConversionException {
		
		File file = new File(getClass().getResource("TaskExecutorServiceProperties.xml"
				).getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		ConsoleCapture capture = new ConsoleCapture();
		try (ConsoleCapture.Close close = capture.captureConsole()) {
		
			oddjob.run();
			
			OddjobLookup lookup = new OddjobLookup(oddjob);
			
			Runnable echo1 = lookup.lookup("echo1", Runnable.class);
			Runnable echo2 = lookup.lookup("echo2", Runnable.class);
			Runnable echo3 = lookup.lookup("echo3", Runnable.class);
			
			ResetActions.HARD.doWith(echo1);
			ResetActions.HARD.doWith(echo2);
			ResetActions.HARD.doWith(echo3);
			
			echo1.run();
			echo2.run();
			echo3.run();
			
		}		
		
		assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
		
		String[] lines = capture.getLines();
		
		assertEquals("Hello Rod.", lines[0].trim());
		assertEquals("Hello Jane.", lines[1].trim());
		assertEquals("Hello Freddy.", lines[2].trim());
		assertEquals("Hello Rod.", lines[3].trim());
		assertEquals("Hello Jane.", lines[4].trim());
		assertEquals("Hello Freddy.", lines[5].trim());
		
		oddjob.destroy();
	}
}
