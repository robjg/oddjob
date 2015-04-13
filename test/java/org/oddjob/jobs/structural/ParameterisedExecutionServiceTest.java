package org.oddjob.jobs.structural;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.input.InputRequest;

public class ParameterisedExecutionServiceTest extends TestCase {

	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		File file = new File(getClass().getResource(
				"ParameterisedExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);
		
		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		ParameterisedExecutionService test = lookup.lookup("parameterised-job", 
				ParameterisedExecutionService.class);
		
		InputRequest[] requests = test.getParameterInfo();
		
		assertEquals(4, requests.length);
		
		Properties properties = new Properties();
		
		properties.setProperty("favourite.fruit", "banana");
		properties.setProperty("favourite.colour", "blue");
		
		test.runWith(properties);
		
		assertEquals(null, test.getProperties());
		
		String text = lookup.lookup("echo.text", String.class);
		
		assertEquals("Favourite Fruit: banana, Favourite Colour: blue", 
				text);
		
		properties.setProperty("favourite.fruit", "kiwi");
		properties.setProperty("favourite.colour", "pink");
		
		test.runWith(properties);
		
		text = lookup.lookup("echo.text", String.class);
		
		assertEquals("Favourite Fruit: kiwi, Favourite Colour: pink", 
				text);
		
		oddjob.destroy();
		
	}
}
