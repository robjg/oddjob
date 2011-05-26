/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * Tests for EnvrionmentType. 
 */
public class PropertiesEnvironmentTest extends TestCase {
	private static final Logger logger = Logger.getLogger(PropertiesEnvironmentTest.class);

	protected void setUp() {
		logger.debug("================== " + getName() + " ===================");
				
	}
		
	/**
	 * Test that a property gets set from the environment..
	 *
	 */
	public void testInOddjob() throws Exception {
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/properties/PropertiesJobEnvironment.xml", 
				getClass().getClassLoader()));
		oj.run();
				
		String text = new OddjobLookup(oj).lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is " + System.getenv("PATH"), text);
		
		oj.destroy();
	}
}
