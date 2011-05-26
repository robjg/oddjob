/**
 * 
 */
package org.oddjob.arooa.types;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

/**
 * @author rob
 *
 */
public class ListTypeExamplesTest extends TestCase {
	private static final Logger logger = Logger.getLogger(
			ListTypeExamplesTest.class);

	
	
	
	public void testFruitExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ListTypeMergeExample.xml",
				getClass().getClassLoader()));

		oddjob.setArgs(new String[] { "kiwis", "mangos" });
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(5, lines.length);
		
		assertEquals("apples", lines[0].trim());
		assertEquals("oranges", lines[1].trim());
		assertEquals("bananas", lines[2].trim());
		assertEquals("kiwis", lines[3].trim());
		assertEquals("mangos", lines[4].trim());
		
		oddjob.destroy();
	}
	
	
	public void testTestConvertExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ListForConversion.xml",
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
