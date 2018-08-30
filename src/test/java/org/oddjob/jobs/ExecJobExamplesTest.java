package org.oddjob.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecJobExamplesTest {
	private static final Logger logger = LoggerFactory.getLogger(ExecJobExamplesTest.class);
		
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
	
}
