package org.oddjob.input;
import org.junit.Before;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.jobs.ExecJob;
import org.oddjob.state.JobState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OurDirs;

public class ConsoleInputHandlerTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(
			ConsoleInputHandlerTest.class);
	
    @Before
    public void setUp() throws Exception {

		
		logger.info("--------------------------  " + getName() + "  -------------------------");
	}
	
   @Test
	public void testMultiplePrompts() {
		
		OurDirs dirs = new OurDirs();
		File example = dirs.relative(
				"test/java/org/oddjob/input/InputHandlerExample.xml");

		String command = "java -jar \"" + dirs.relative("run-oddjob.jar"
				).getPath() + "\" -f \"" + example + "\"";
		
		String input = "/bin/oddjob\n" +
				"\n" +
				"robbo\n" +
				"secret\n" +
				"y\n" +
				"\n";
		
		ExecJob exec = new ExecJob();
		exec.setCommand(command);
		exec.setStdin(new ByteArrayInputStream(input.getBytes()));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.capture(exec.consoleLog())) {
			
			exec.run();
		}
		
		console.dump(logger);
		
		assertEquals(JobState.COMPLETE, 
				exec.lastStateEvent().getState());
		
		String[] lines = console.getLines();
		
		assertEquals("Install Directory? (/home/oddjob/foo)", lines[0].trim());
		assertEquals("System? (Development)", lines[1].trim());
		assertEquals("Username?", lines[2].trim());
		assertEquals("Password?", lines[3].trim());
		assertEquals("Agree To Licence (Yes/No)? (No)", lines[4].trim());
		assertEquals("Password for robbo is secret", lines[5].trim());
		assertEquals("Logging On to Development Now! (Return To Continue)", lines[6].trim());
		
		assertEquals(7, lines.length);
		
		exec.destroy();
	}
}
