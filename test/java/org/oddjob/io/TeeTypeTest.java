package org.oddjob.io;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.util.Properties;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OurDirs;

public class TeeTypeTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(TeeTypeTest.class);
	
	File workDir;

	File testFile;
	
   @Before
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
	
   @Test
	public void testOutputStreamExample() {
		
		File config = new File(getClass().getResource(
				"TeeTypeOutputStream.xml").getFile());
		
		Properties properties = new Properties();
		properties.setProperty("work.dir", workDir.getAbsolutePath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(config);
		oddjob.setProperties(properties);
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		assertEquals("Duplicate This!", console.getLines()[0].trim());
		
		assertEquals(true, testFile.exists());
		
		oddjob.destroy();
	}
	
   @Test
	public void testInputStreamExample() {
		
		File config = new File(getClass().getResource(
				"TeeTypeInputStream.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(config);
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		assertEquals("This will be copied when read.", console.getLines()[0].trim());
		
		oddjob.destroy();
	}
}
