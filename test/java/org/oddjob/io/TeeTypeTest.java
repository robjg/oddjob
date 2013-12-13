package org.oddjob.io;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OurDirs;

public class TeeTypeTest extends TestCase {

	private static final Logger logger = Logger.getLogger(TeeTypeTest.class);
	
	File workDir;

	File testFile;
	
	public void setUp() throws Exception {
		
		logger.info("----------------------  " + getName() + 
				"  ----------------------");
		
		OurDirs dirs = new OurDirs();
		
		workDir = new File(dirs.base(), "work/io");
		
		if (!workDir.exists()) {
			workDir.mkdirs();
		}
		
		testFile = new File(workDir, "TeeTypeTest.txt");
		testFile.delete();
	}
	
	public void testOutputStreamExample() {
		
		File config = new File(getClass().getResource(
				"TeeTypeOutputStream.xml").getFile());
		
		Properties properties = new Properties();
		properties.setProperty("work.dir", workDir.getAbsolutePath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(config);
		oddjob.setProperties(properties);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		console.close();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		assertEquals("Duplicate This!", console.getLines()[0].trim());
		
		assertEquals(true, testFile.exists());
		
		oddjob.destroy();
	}
	
	public void testInputStreamExample() {
		
		File config = new File(getClass().getResource(
				"TeeTypeInputStream.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(config);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		console.close();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		assertEquals("This will be copied when read.", console.getLines()[0].trim());
		
		oddjob.destroy();
	}
}
