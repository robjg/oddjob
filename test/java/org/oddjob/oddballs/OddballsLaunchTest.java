package org.oddjob.oddballs;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.jobs.ExecJob;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.tools.OurDirs;

public class OddballsLaunchTest extends OjTestCase {
	private static final Logger logger = Logger.getLogger(
			OddballsLaunchTest.class);
	
	final static String RUN_JAR = "run-oddjob.jar";
	
	final static String EOL = System.getProperty("line.separator");
	
    @Before
    public void setUp() throws Exception {
		logger.info("---------------- " + getName() + " -----------------");
		
		File built = new File(new OurDirs().base(), RUN_JAR);
		
		assertTrue(built.exists());
		
	}
	
	class Console implements LogListener  {
		List<String> lines = new ArrayList<String>();
		
		public void logEvent(LogEvent logEvent) {
			lines.add(logEvent.getMessage());
		}
	}
	
   @Test
	public void testOddjobFailsNoFile() throws InterruptedException {
		
		Console console = new Console();
		
		OurDirs dirs = new OurDirs();
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar \"" + dirs.relative(RUN_JAR).getPath() + "\"" + 
				" -ob \"" + dirs.relative("/test/oddballs").getPath() + "\"" +
				" -f \"" + dirs.relative("/test/launch/oddballs-launch.xml") + "\"");
		
		exec.consoleLog().addListener(console, LogLevel.INFO, -1, 1000);
		
		exec.run();

		dump(console.lines);
		
		assertEquals("Apple: Red" + EOL, console.lines.get(0));
		assertEquals("Colour As String: Red" + EOL, console.lines.get(1));
		
		exec.destroy();
	}
	
	
	void dump(List<String> lines) {
		System.out.println("******************");
		for (String line : lines) {
			System.out.print(line);
		}
		System.out.println("******************");
	}
	
}
