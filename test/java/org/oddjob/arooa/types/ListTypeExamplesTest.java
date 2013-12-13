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
public class ListTypeExamplesTest extends TestCase {
	private static final Logger logger = Logger.getLogger(
			ListTypeExamplesTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-----------------------------  " + getName() 
				+ "  --------------------------------");
	}
	
	public void testSimpleWithNestedListExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ListSimpleWithNestedList.xml",
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
				).getResourceAsStream("ListSimpleWithNestedListOut.txt"));
		
		assertEquals(expected[0], lines[0].trim());
		assertEquals(expected[1], lines[1].trim());
		assertEquals(expected[2], lines[2].trim());
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();
	}
	
	
	public void testMergeFruitExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ListTypeMergeExample.xml",
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
				).getResourceAsStream("ListTypeMergeExampleOut.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(5, lines.length);
		
		oddjob.destroy();
	}
	
	
	public void testTestConvertExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ListWithConversion.xml",
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
				).getResourceAsStream("ListWithConversionOut.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(5, lines.length);
		
		oddjob.destroy();
	}
	
	public void testAddingToAListOnTheFly() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ListTypeAddWithSet.xml",
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
				).getResourceAsStream("ListTypeAddWithSetOut.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(2, lines.length);
		
		oddjob.destroy();
	}
}
