package org.oddjob.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;

public class PathParserLaunchTest extends TestCase {

	final static String RUN_JAR = "run-oddjob.jar";
	
	@Override
	protected void setUp() throws Exception {
		File built = new File(new OurDirs().base(), RUN_JAR);
		
		assertTrue(built.exists());
		
	}
	
	public static class Test implements Runnable {
		
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
	
	public void testWithLaunch() throws ArooaConversionException, IOException {

		OurDirs dirs = new OurDirs();
		
		File buildTest = new File(dirs.base(), "build/test");
		assertTrue("Tests must have been built with ant.", buildTest.exists());
		
		String xml = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <exec id='exec'>" +
			" java -jar " + new File(dirs.base(), RUN_JAR) + 
			" -cp ${this.args[0]}/build/test" +
			" -l ${this.args[0]}/test/launch/log4j.properties" +
			" -f ${this.args[0]}/test/launch/classpath-test1.xml" +
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
