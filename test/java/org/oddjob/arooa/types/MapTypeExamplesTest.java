/**
 * 
 */
package org.oddjob.arooa.types;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.OjTestCase;

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
public class MapTypeExamplesTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(
			MapTypeExamplesTest.class);

	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }

   @Before
   public void setUp() throws Exception {

		
		logger.info("-----------------------------  " + getName() 
				+ "  --------------------------------");
	}
	
   @Test
	public void testMapElementAccessExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/MapElementTest.xml",
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
				).getResourceAsStream("MapElementTest.txt"));
		
		assertEquals(expected[0], lines[0].trim());
		
		assertEquals(1, lines.length);
		
		oddjob.destroy();
	}
	
	
   @Test
	public void testAddingToAListOnTheFly() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/MapTypeAddWithSet.xml",
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
				).getResourceAsStream("MapTypeAddWithSet.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(2, lines.length);
		
		oddjob.destroy();
	}
}
