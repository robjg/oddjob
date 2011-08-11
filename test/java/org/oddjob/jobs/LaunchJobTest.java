package org.oddjob.jobs;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.launch.Launcher;
import org.oddjob.state.ParentState;

public class LaunchJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(LaunchJobTest.class);
	
    public void testLaunchAsjobInOddjob() throws IOException {
    	
    	ClassLoader existingContext = Thread.currentThread(
    			).getContextClassLoader();
    	Thread.currentThread().setContextClassLoader(null);
    	
		OurDirs dirs = new OurDirs();
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/jobs/LaunchExample.xml",
    			getClass().getClassLoader()));
		oddjob.setArgs(new String[] { 
				dirs.base().toString(),
				Launcher.ODDJOB_MAIN_CLASS } );
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		assertEquals(1, lines.length);
		assertTrue(lines[0].startsWith("URLClassLoader:"));
		
		oddjob.destroy();
		
    	Thread.currentThread().setContextClassLoader(existingContext);
    }

	
}
