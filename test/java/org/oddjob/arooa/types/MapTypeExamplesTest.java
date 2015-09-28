/**
 * 
 */
package org.oddjob.arooa.types;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

/**
 * @author rob
 *
 */
public class MapTypeExamplesTest extends TestCase {
	private static final Logger logger = Logger.getLogger(
			MapTypeExamplesTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-----------------------------  " + getName() 
				+ "  --------------------------------");
	}
	
	public void testMapElementAccessExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/MapElementTest.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("MapElementTest.txt"));
		
		assertEquals(expected[0], lines[0].trim());
		
		assertEquals(1, lines.length);
		
		oddjob.destroy();
	}
	
	
	public void testAddingToAListOnTheFly() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/MapTypeAddWithSet.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		console.captureConsole();
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("MapTypeAddWithSet.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(2, lines.length);
		
		oddjob.destroy();
	}
}
