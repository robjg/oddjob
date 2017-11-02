package org.oddjob.arooa.types;

import org.junit.Test;

import java.io.File;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

public class ConvertTypeExamplesTest extends OjTestCase {
	
	private static final Logger logger = Logger.getLogger(
			ConvertTypeExamplesTest.class);

   @Test
	public void testFruitExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/ConvertDelimitedTextToArray.xml",
				getClass().getClassLoader()));

		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
			
			assertEquals(ParentState.COMPLETE, 
					oddjob.lastStateEvent().getState());
			
		}
				
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("ConvertDelimitedTextToArray.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();
	}
	
   @Test
	public void testIsPropertyUsage() {
		
		File file = new File(getClass().getResource(
				"ConvertIsPropertyUsage.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);

		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {

			oddjob.run();

			assertEquals(ParentState.COMPLETE, 
					oddjob.lastStateEvent().getState());
		}
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("ConvertIsPropertyUsage.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], lines[i].trim());
		}
		
		assertEquals(6, lines.length);
		
		oddjob.destroy();
	}
}
