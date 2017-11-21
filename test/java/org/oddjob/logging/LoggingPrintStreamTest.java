/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.cache.LogArchiveImpl;

/**
 * 
 */
public class LoggingPrintStreamTest extends OjTestCase {

	PrintStream test;
	
	final List<String> text = new ArrayList<String>();
	LogLevel level;
	
	class MyLL implements LogListener {
		
		public void logEvent(LogEvent logEvent) {
			level = logEvent.getLevel();
			text.add(logEvent.getMessage());
		}
	}
	
   @Before
   public void setUp() {
		LogArchiveImpl logArchive = new LogArchiveImpl("whatever", 10);
		test = new LoggingPrintStream(System.out, LogLevel.WARN, logArchive);
		level = null;
		logArchive.addListener(new  MyLL(), LogLevel.DEBUG, -1, 1000);
	}
	
   @Test
	public void testPrintlnString() {
		test.println("Hello");
		
		assertEquals(1, text.size());
		assertEquals("Hello" + System.getProperty("line.separator"), 
				text.get(0));
		
		assertEquals(LogLevel.WARN, level);
	}
	
   @Test
	public void testLn() throws IOException {
		test.print("Hello");
		assertEquals(0, text.size());
		test.println();
		
		assertEquals(1, text.size());
		assertEquals("Hello" + System.getProperty("line.separator"), 
				text.get(0));
	}
}
