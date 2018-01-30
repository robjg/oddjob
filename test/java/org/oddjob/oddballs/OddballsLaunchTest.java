package org.oddjob.oddballs;
import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.jobs.ExecJob;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OurDirs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OddballsLaunchTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(
			OddballsLaunchTest.class);
	
	final static String RUN_JAR = "run-oddjob.jar";
	
    @Before
    public void setUp() throws Exception {
		logger.info("---------------- " + getName() + " -----------------");
		
		File built = new File(new OurDirs().base(), RUN_JAR);
		
		assertTrue(built.exists());
		
	}
	
   @Test
	public void testOddjobFailsNoFile() throws InterruptedException {
		
		OurDirs dirs = new OurDirs();
		
		ExecJob exec = new ExecJob();
		exec.setCommand("java -jar \"" + dirs.relative(RUN_JAR).getPath() + "\"" + 
				" -ob \"" + dirs.relative("/test/oddballs").getPath() + "\"" +
				" -f \"" + dirs.relative("/test/launch/oddballs-launch.xml") + "\"" +
				" -l \"" + dirs.relative(OjTestCase.logConfig()) + "\"");

		ConsoleCapture capture = new ConsoleCapture();
		try (ConsoleCapture.Close close = capture.capture(exec.consoleLog())) {
			exec.run();
		}
		
		capture.dump();

		String[] lines = capture.getLines();
		
		assertEquals("Apple: Red", lines[0]);
		assertEquals("Colour As String: Red", lines[1]);
		
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
