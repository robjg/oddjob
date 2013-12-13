package org.oddjob.arooa.types;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

import junit.framework.TestCase;

public class ValueTypeExamplesTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ValueTypeExamplesTest.class);

	public void testValueTypeInternalsExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ValueTypeInternalsExample.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
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
