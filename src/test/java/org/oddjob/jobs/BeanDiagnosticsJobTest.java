package org.oddjob.jobs;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;

public class BeanDiagnosticsJobTest extends OjTestCase {

	String EOL = System.getProperty("line.separator");
	
   @Test
	public void testInOddjob() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/BeanDiagnosticsJobTest.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		String expected = 			
			"Analysed 1 beans. Discovered 1 types." + EOL +
			"Type: SimpleArooaClass: class java.lang.Object" + EOL +
			" Properties:" + EOL +
			"  class: java.lang.Class (Read Only)" + EOL;
		
		String results = new OddjobLookup(oddjob).lookup(
				"results-buffer", String.class); 

		assertEquals(expected, results);
		
		oddjob.destroy();
	}
	
}
