package org.oddjob.launch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.tools.OurDirs;

public class PathParserLaunchTest {

	final static String RUN_JAR = "run-oddjob.jar";
	
    @Before
    public void setUp() throws Exception {
		File built = new File(new OurDirs().base(), RUN_JAR);
		
		assertTrue(built.exists());
		
	}
	
	public static class TestJob implements Runnable {
		
		public void run() {
			System.out.println("That Worked.");
		}
	}
	
	private class LogCatcher implements LogListener {
		
		List<String> lines = new ArrayList<String>();
		
		public void logEvent(LogEvent logEvent) {
			lines.add(logEvent.getMessage());
		}
	}
	
	static String EOL = System.getProperty("line.separator");
	
    @Test
	public void testWithLaunch() throws ArooaConversionException, IOException {

		OurDirs dirs = new OurDirs();
		
		File buildTest = new File(dirs.base(), "build/test/classes");
		assertTrue("Tests must have been built with ant.", buildTest.exists());

		String logConfig = OjTestCase.logConfig();

		String xml = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <exec id='exec'>" +
			" java -jar \"" + new File(dirs.base(), RUN_JAR).getPath() + "\"" +
			" -cp \"${this.args[0]}/build/test/classes\"" +
			" -l \"${this.args[0]}/" + logConfig +"\"" +
			" -f \"${this.args[0]}/test/launch/classpath-test1.xml\"" +
			"  </exec>" +
			" </job>" +
			"</oddjob>";

		Oddjob oddjob = new Oddjob();
		oddjob.setArgs(new String[] { dirs.base().toString() });
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		ConsoleOwner archive = new OddjobLookup(oddjob).lookup(
				"exec", 
				ConsoleOwner.class);
		
		LogCatcher log = new LogCatcher();
		
		archive.consoleLog().addListener(log, LogLevel.INFO, -1, 100);
		
		System.out.println("*****************************");
		for (String line: log.lines) {
			System.out.print(line);
		}
		
		assertTrue(log.lines.get(0).contains(new File(dirs.base(), "build/test").getCanonicalPath()));
		assertEquals(dirs.base().toString(), log.lines.get(1).trim());
		assertEquals("That Worked." + EOL, log.lines.get(2));
	}
	
	
}
