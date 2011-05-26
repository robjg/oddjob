package org.oddjob.arooa.types;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

import junit.framework.TestCase;

public class ConvertTypeExamplesTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(
			ConvertTypeExamplesTest.class);

	public void testFruitExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ConvertDelimitedTextToArray.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("grapes, red", lines[0].trim());
		assertEquals("grapes, white", lines[1].trim());
		assertEquals("gratefruit", lines[2].trim());
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();
	}
}
