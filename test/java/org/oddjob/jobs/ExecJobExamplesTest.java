package org.oddjob.jobs;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

public class ExecJobExamplesTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ExecJobExamplesTest.class);
	
	public void testEnvironmentExample() throws ArooaPropertyException, ArooaConversionException {
	
		String envCommand;
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.matches(".*windows.*")) {
			envCommand = "cmd /c set";
		}
		else {
			envCommand = "sh -c set";
		}

		Properties properties = new Properties();
		properties.put("platform.set.command", envCommand);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/ExecJobEnvironmentExample.xml",
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
		
		oddjob.load();
		
		ExecJob exec = new OddjobLookup(oddjob).lookup("exec", ExecJob.class);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(exec.consoleLog());
				
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		boolean found = false;
		for (String line : lines) {
			if (line.contains("ODDJOB_FILE=myfile.txt")) {
				found = true;
			}
		}
		
		assertTrue(found);
		
		oddjob.destroy();
	}
		
	public void testWithStdInExample() throws ArooaPropertyException, ArooaConversionException, IOException {
		
		OurDirs dirs = new OurDirs();
		File runJar = dirs.relative("run-oddjob.jar");
		
		Properties properties = new Properties();
		properties.put("oddjob.run.jar", runJar.getCanonicalPath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(dirs.relative(
				"test/java/org/oddjob/jobs/ExecWithStdInExample.xml"));
		oddjob.setProperties(properties);
		
		oddjob.load();
		
		ExecJob exec = new OddjobLookup(oddjob).lookup("exec", ExecJob.class);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(exec.consoleLog());
				
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("apples", lines[0].trim());
		assertEquals("oranges", lines[1].trim());
		assertEquals("pears", lines[2].trim());
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();
	}
	
}
