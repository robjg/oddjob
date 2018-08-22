/**
 * 
 */
package org.oddjob.arooa.beanutils;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

/**
 * @author rob
 *
 */
public class MagicBeansExamplesTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(
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
		 
		assertEquals("Checking Big File (" + fs + "files" + fs + "big)", lines[1]);
		assertEquals("less than 1000000 bytes...", lines[2]);
		assertEquals("Checking Medium File (" + fs + "files" + fs + "medium)", lines[4]);
		assertEquals("less than 20000 bytes...", lines[5]);
		assertEquals("Checking Small File (" + fs + "files" + fs + "small)", lines[7]);
		assertEquals("less than 3000 bytes...", lines[8]);
		
		assertEquals(9, lines.length);
		
		oddjob.destroy();
	}
	
}
