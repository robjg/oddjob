/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.OjTestCase;

import org.oddjob.logging.cache.LogArchiveImpl;

public class LogArchiveImplTest extends OjTestCase {

   @Test
	public void testFullArchive() {
		class MyL implements LogListener {
			String previous;
			String message;
			public void logEvent(LogEvent logEvent) {
				previous = message;
				message = logEvent.getMessage();
				
			}
		}
		MyL l = new MyL();
		
		LogArchiveImpl test = new LogArchiveImpl("foo", 1);
		test.addListener(l, LogLevel.DEBUG, 0, 1);
		
		test.addEvent(LogLevel.DEBUG, "1");
		assertEquals("1", l.message);
		
		test.addEvent(LogLevel.DEBUG, "2");
		assertEquals("2", l.message);
		
		test.addEvent(LogLevel.DEBUG, "3");
		assertEquals("3", l.message);
		
		MyL l2 = new MyL();
		test.addListener(l2, LogLevel.DEBUG, 0, 1000);
		assertEquals("3", l2.message);
		
		// check that there really was only one message in the buffer.
		assertNull(l2.previous);
	}
	
   @Test
	public void testOneMessage() {
		class MyL implements LogListener {
			List<LogEvent> results = new ArrayList<LogEvent>();
			public void logEvent(LogEvent logEvent) {
				results.add(logEvent);
			}
		}
		
		LogArchiveImpl test = new LogArchiveImpl("foo", 1000);
		
		test.addEvent(LogLevel.DEBUG, "1");
		
		MyL l = new MyL();
		
		test.addListener(l, LogLevel.DEBUG, -1, 1000);
		
		assertEquals(1, l.results.size());
		assertEquals(0, l.results.get(0).getNumber());
		
		MyL l2 = new MyL();
		
		test.addListener(l2, LogLevel.DEBUG, 0, 1000);
		
		assertEquals(0, l2.results.size());
		
	}
	
   @Test
	public void testFromMessage() {
		class MyL implements LogListener {
			List<LogEvent> results = new ArrayList<LogEvent>();
			public void logEvent(LogEvent logEvent) {
				results.add(logEvent);
			}
		}
		
		LogArchiveImpl test = new LogArchiveImpl("foo", 1000);
		
		test.addEvent(LogLevel.DEBUG, "1");
		test.addEvent(LogLevel.DEBUG, "2");
		test.addEvent(LogLevel.DEBUG, "3");
		test.addEvent(LogLevel.DEBUG, "4");
		test.addEvent(LogLevel.DEBUG, "5");
		test.addEvent(LogLevel.DEBUG, "6");
		test.addEvent(LogLevel.DEBUG, "7");
		
		MyL l = new MyL();
		
		test.addListener(l, LogLevel.DEBUG, 3, 2);
		
		assertEquals(2, l.results.size());
		assertEquals("7", l.results.get(1).getMessage());
		
		MyL l2 = new MyL();
		
		test.addListener(l2, LogLevel.DEBUG, 6, 2);
		
		assertEquals(0, l2.results.size());
		
		test.removeListener(l2);
		// test big from
		
		test.addListener(l2, LogLevel.DEBUG, 20, 2);
		
		assertEquals(0, l2.results.size());
		
	}
	
   @Test
	public void testLevel() {
		class MyL implements LogListener {
			List<LogEvent> results = new ArrayList<LogEvent>();
			public void logEvent(LogEvent logEvent) {
				results.add(logEvent);
			}
		}
		
		LogArchiveImpl test = new LogArchiveImpl("foo", 1000);
		
		test.addEvent(LogLevel.DEBUG, "1");
		test.addEvent(LogLevel.INFO, "2");
		test.addEvent(LogLevel.WARN, "3");
		test.addEvent(LogLevel.ERROR, "4");
		test.addEvent(LogLevel.FATAL, "5");
		
		MyL l = new MyL();
		
		test.addListener(l, LogLevel.DEBUG, -1, 1000);
		
		assertEquals(5, l.results.size());
		assertEquals("5", l.results.get(4).getMessage());
		
		MyL l2 = new MyL();
		
		test.addListener(l2, LogLevel.INFO, -1, 1000);
		
		assertEquals(4, l2.results.size());
		assertEquals("5", l2.results.get(3).getMessage());
		
		MyL l3 = new MyL();
		
		test.addListener(l3, LogLevel.WARN, -1, 1000);
		
		assertEquals(3, l3.results.size());
		assertEquals("5", l3.results.get(2).getMessage());
		
		MyL l4 = new MyL();
		
		test.addListener(l4, LogLevel.ERROR, -1, 1000);
		
		assertEquals(2, l4.results.size());
		assertEquals("5", l4.results.get(1).getMessage());
		
		MyL l5 = new MyL();
		
		test.addListener(l5, LogLevel.FATAL, -1, 1000);
		
		assertEquals(1, l5.results.size());
		assertEquals("5", l5.results.get(0).getMessage());
		
	}
	
}
