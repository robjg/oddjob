package org.oddjob.jobs;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.OjTestCase;

import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.tools.OurDirs;

public class ExecJobBatTest extends OjTestCase {

	class Console implements LogListener {

		List<String> lines = new ArrayList<String>();
		
		public void logEvent(LogEvent logEvent) {
			lines.add(logEvent.getMessage());
		}
	}
	
   @Test
	public void testConsole() throws IOException {
	
		if (!System.getProperty("os.name").startsWith("Windows")) {
			return;
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
				
		ExecJob test = new ExecJob();
		test.setStdout(out);
		test.setDir(new OurDirs().relative("test"));
		test.setCommand("cmd /q /c test.bat");
		test.run();
				
		assertEquals("hello\r\ngoodbye\r\n", out.toString());
		
		Console console = new Console();
		test.consoleLog().addListener(console, LogLevel.DEBUG, -1, 1000);

		dump(console.lines);
		
		assertEquals(2, console.lines.size());

		assertEquals("hello" + System.getProperty("line.separator"), 
				console.lines.get(0));		
	}
	

	void dump(List<String> lines) {
		System.out.println("******************");
		for (String line : lines) {
			System.out.print(line);
		}
	}
	
}
