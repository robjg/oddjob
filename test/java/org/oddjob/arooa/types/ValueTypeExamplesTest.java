package org.oddjob.arooa.types;

import org.junit.Test;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

import org.oddjob.OjTestCase;

public class ValueTypeExamplesTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(ValueTypeExamplesTest.class);

   @Test
	public void testValueTypeInternalsExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ValueTypeInternalsExample.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("ValueTypeInternalsExampleOut.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(6, lines.length);
		
		oddjob.destroy();
	}
	
}
