package org.oddjob.jobs.tasks;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.input.InputRequest;
import org.oddjob.jobs.tasks.TaskExecutionService;

public class TaskExecutionServiceTest extends TestCase {

	public void testExample() throws ArooaPropertyException, ArooaConversionException, TaskException {
		
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
		
		test.execute(properties);
		
		assertEquals(null, test.getProperties());
		
		String text = lookup.lookup("echo.text", String.class);
		
		assertEquals("Favourite Fruit: banana, Favourite Colour: blue", 
				text);
		
		properties.setProperty("favourite.fruit", "kiwi");
		properties.setProperty("favourite.colour", "pink");
		
		test.execute(properties);
		
		text = lookup.lookup("echo.text", String.class);
		
		assertEquals("Favourite Fruit: kiwi, Favourite Colour: pink", 
				text);
		
		oddjob.destroy();
		
	}
}
