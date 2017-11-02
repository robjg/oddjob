/**
 * 
 */
package org.oddjob.arooa.beanutils;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

/**
 * @author rob
 *
 */
public class MagicBeansExamplesTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(
			MagicBeansExamplesTest.class);

	
   @Test
	public void testFileSizesExample() {
		
		URL url = getClass().getResource("MagicBeansExample.xml");
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(new File(url.getFile()));

		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		String fs = File.separator;
		 
		assertEquals("Checking Big File (" + fs + "files" + fs + "big)", lines[1].trim());
		assertEquals("less than 1000000 bytes...", lines[2].trim());
		assertEquals("Checking Medium File (" + fs + "files" + fs + "medium)", lines[4].trim());
		assertEquals("less than 20000 bytes...", lines[5].trim());
		assertEquals("Checking Small File (" + fs + "files" + fs + "small)", lines[7].trim());
		assertEquals("less than 3000 bytes...", lines[8].trim());
		
		assertEquals(9, lines.length);
		
		oddjob.destroy();
	}
	
}
