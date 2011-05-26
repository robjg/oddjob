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
 * Test System Properties.
 */
public class PropertiesJobSystemTest extends TestCase {
	private static final Logger logger = Logger.getLogger(PropertiesJobSystemTest.class);
	
	protected void setUp() {
		
		logger.debug("--------------- " + getName() + " -----------------" );
		
		if (System.getProperty("oddjob.test") != null) {
			throw new IllegalStateException("Property in use already.");
		}
		System.setProperty("oddjob.test", "Test");
	}
	
	@Override
	protected void tearDown() throws Exception {
		System.getProperties().remove("oddjob.test");
	}
		
	public void testSystemPropertyInOddjob() throws Exception {

		String xml=
			"<oddjob>" +
			" <job>" +
			"  <echo id='echo' text='${oddjob.test}'/>" +
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
			"    <echo id='echo' text='Result: ${oddjob.test}'/>" +
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
