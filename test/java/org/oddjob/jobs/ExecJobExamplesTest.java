package org.oddjob.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.AppenderAdapter;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.logging.LoggingEvent;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.logging.slf4j.LogoutType;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecJobExamplesTest {
	private static final Logger logger = LoggerFactory.getLogger(ExecJobExamplesTest.class);
	
	// Do not use oddjob.run.jar as it's set by the Launcher test if it runs first.
	static final String ODDJOB_RUN_JAR_PROPERTY = "oddjob.test.run.jar";
	
	@Rule public TestName name = new TestName();

	@Before
	public void setUp() {
        logger.info("---------------------  " + name.getMethodName() + "  -------------------");
    }

	
	@Test
	public void testSimpleExamples() throws ArooaParseException {
		
		// Just check the XML.
		
		OddjobTestHelper.createComponentFromConfiguration(
					new XMLConfiguration("org/oddjob/jobs/ExecSimpleExample.xml",
					getClass().getClassLoader()));
		
		OddjobTestHelper.createComponentFromConfiguration(
				new XMLConfiguration("org/oddjob/jobs/ExecSimpleExample2.xml",
				getClass().getClassLoader()));
		
	}
	
    @Test
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
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
			oddjob.run();
		}
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
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
		
    @Test
	public void testWithStdInExample() throws ArooaPropertyException, ArooaConversionException, IOException {
		
		OurDirs dirs = new OurDirs();
		File runJar = dirs.relative("run-oddjob.jar");
		
		logger.info("Setting {} to {}", ODDJOB_RUN_JAR_PROPERTY, runJar.getCanonicalPath());
		
		Properties properties = new Properties();
		properties.put(ODDJOB_RUN_JAR_PROPERTY, runJar.getCanonicalPath());
		properties.put("logConfigArgs", "-l " + dirs.relative(OjTestCase.logConfig()));
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(dirs.relative(
				"test/java/org/oddjob/jobs/ExecWithStdInExample.xml"));
		oddjob.setProperties(properties);
		
		oddjob.load();
		
		ExecJob exec = new OddjobLookup(oddjob).lookup("exec", ExecJob.class);
		
		ConsoleCapture console = new ConsoleCapture();
		console.setLeaveLogging(true);
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
			oddjob.run();
		}
		
		console.dump(logger);
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		String[] lines = console.getLines();
		
		assertEquals("apples", lines[0].trim());
		assertEquals("oranges", lines[1].trim());
		assertEquals("pears", lines[2].trim());
		
		assertEquals(3, lines.length);
		
		oddjob.destroy();
	}
	
    @Test
	public void testWithRedirectToFileExample() throws ArooaPropertyException, ArooaConversionException, IOException {
		
		OurDirs dirs = new OurDirs();
		File runJar = dirs.relative("run-oddjob.jar");
		File workDir = dirs.relative("work");
		File output = new File(workDir, "ExecOutput.log");
		if (output.exists()) {
			output.delete();
		}
		
		Properties properties = new Properties();
		properties.put(ODDJOB_RUN_JAR_PROPERTY, runJar.getCanonicalPath());
		properties.put("work.dir", workDir.getCanonicalPath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(dirs.relative(
				"test/java/org/oddjob/jobs/ExecWithRedirectToFile.xml"));
		oddjob.setProperties(properties);

		oddjob.load();
		
		ExecJob exec = new OddjobLookup(oddjob).lookup("exec", ExecJob.class);
		
		ConsoleCapture console = new ConsoleCapture();
		console.setLeaveLogging(true);
		
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
			oddjob.run();
		}
		
		console.dump(logger);
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
			
		oddjob.destroy();
	}
	
	
	private class Results implements Appender {
		
		List<Object> info = new ArrayList<Object>();
		List<Object> warn = new ArrayList<Object>();
		
		@Override
		public void append(LoggingEvent arg0) {
			if (arg0.getLevel() == LogLevel.INFO) {
				info.add(arg0.getMessage());
			}
			if (arg0.getLevel() == (LogLevel.WARN)) {
				warn.add(arg0.getMessage());
			}
		}
	}
	
   @Test
	public void testWithRedirectToLogExample() throws IOException {
		
		OurDirs dirs = new OurDirs();
		File runJar = dirs.relative("run-oddjob.jar");
		
		Properties properties = new Properties();
		properties.put(ODDJOB_RUN_JAR_PROPERTY, runJar.getCanonicalPath());
		properties.put("logConfigArgs", "-l " + dirs.relative(OjTestCase.logConfig()));
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/ExecWithRedirectToLog.xml", 
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
		
		Results results = new Results();
		
		AppenderAdapter appenderAdapter = LoggerAdapter.appenderAdapterFor(LogoutType.class);
		
		appenderAdapter.addAppender(results);
		
		oddjob.run();
				
		appenderAdapter.removeAppender(results);
		
		assertEquals(ParentState.INCOMPLETE,
				oddjob.lastStateEvent().getState());
		
		assertTrue(results.info.size() == 0);		
		assertTrue(results.warn.size() > 0);		
		
		oddjob.destroy();		
	}
	
}
