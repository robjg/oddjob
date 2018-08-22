/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.properties;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * Test System Properties.
 */
public class PropertiesJobSystemTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesJobSystemTest.class);
	
   @Before
   public void setUp() {
		
		logger.debug("--------------- " + getName() + " -----------------" );
		
		if (System.getProperty("oddjob.test") != null) {
			throw new IllegalStateException("Property in use already.");
		}
		System.setProperty("oddjob.test", "Test");
	}
	
    @After
    public void tearDown() throws Exception {
		System.getProperties().remove("oddjob.test");
	}
		
   @Test
	public void testSystemPropertyInOddjob() throws Exception {

		String xml=
			"<oddjob>" +
			" <job>" +
			"  <echo id='echo'>${oddjob.test}</echo>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		String result = new OddjobLookup(oj).lookup(
				"echo.text", String.class);
		
		assertEquals("Test", result);
		
		oj.destroy();
	}
	

	
   @Test
	public void testSettingAllInOddjob() throws Exception {
		
		String xml=
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <properties id='props'>" +
			"     <values>" +
			"      <value key='oddjob.test' value='Different'/>" +
			"     </values>" +
			"    </properties>" +
			"    <echo id='echo'>Result: ${oddjob.test}</echo>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		OddjobLookup lookup = new OddjobLookup(oj);
		
		String result = lookup.lookup(
				"echo.text", String.class);
		
		assertEquals("Result: Test", result);
		
		assertEquals("Different", lookup.lookup(
				"props.properties(oddjob.test)"));
		
		oj.destroy();
	}

}
