/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.properties;
import org.junit.Before;

import org.junit.Test;

import java.util.Map;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Describable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * Tests for EnvrionmentType. 
 */
public class PropertiesEnvironmentTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesEnvironmentTest.class);

   @Before
   public void setUp() {
		logger.debug("================== " + getName() + " ===================");
				
	}
		
	/**
	 * Test that a property gets set from the environment..
	 *
	 */
   @Test
	public void testInOddjob() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/properties/PropertiesJobEnvironment.xml", 
				getClass().getClassLoader()));
		
		oddjob.load();

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object echo = lookup.lookup("echo-path");
		((Runnable) echo).run();
		
		String text = lookup.lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is ", text);
		
		((Resetable) echo).hardReset();
		
		oddjob.run();
		
		text = lookup.lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is " + System.getenv("PATH"), text);
		
		Object test = lookup.lookup("props");
		
		Map<String, String> description = 
				((Describable) test).describe();
		
		assertTrue(description.size() > 0);
		
		((Resetable) test).hardReset();
		((Resetable) echo).hardReset();
		((Runnable) echo).run();
		
		text = lookup.lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is ", text);
		
		oddjob.destroy();
	}
}
