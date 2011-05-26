package org.oddjob.logging.polling;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.jmx.client.MockLogPollable;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.cache.LogEventSource;
import org.oddjob.logging.cache.PollingLogArchiver;

/**
 * More tests for the basics - tracking down a bug.
 */
public class PollingLogArchiver2Test extends TestCase {

	Object expected;
	
	class OurEventSource implements LogEventSource {

		LogEvent[] logEvents = {
			new LogEvent("thing", 24L, LogLevel.DEBUG, "1"),
			new LogEvent("thing", 25L, LogLevel.DEBUG, "2"),
			new LogEvent("thing", 26L, LogLevel.DEBUG, "3"),
			new LogEvent("thing", 27L, LogLevel.DEBUG, "4"),
			new LogEvent("thing", 28L, LogLevel.DEBUG, "5")
		};

		
		public LogEvent[] retrieveEvents(Object component, long from, int max) {
			
			assertEquals(expected, component);
			
			if (from < 0) {
				from = 23;
			}
			
			long num = Math.min(max, 28 - from);
			LogEvent[] out = new LogEvent[(int) num];
			System.arraycopy(logEvents, (int) from - 23, out, 0, (int) num);
			return out;
		}
		
	}
	
	class LL implements LogListener {
		List<LogEvent> results = new ArrayList<LogEvent>();
		
		public void logEvent(LogEvent logEvent) {
			results.add(logEvent);
		}
	}
	
	class OurResolver implements ArchiveNameResolver {
		public String resolveName(Object component) {
			assertEquals(expected, component);
			return "apple.log";
		}
	}
	
	public void testSimpleLogEvents() {
		
		MockLogPollable root = new MockLogPollable();
		
		this.expected = root;
		
		PollingLogArchiver test = new PollingLogArchiver(
				3, new OurResolver(), new OurEventSource());

		LL results = new LL();
		
		test.addLogListener(results, root, LogLevel.DEBUG, -1, 10);
		
		assertEquals(3, results.results.size());
		assertEquals("3", results.results.get(2).getMessage());
		
		test.poll();

		assertEquals(5, results.results.size());
		assertEquals("5", results.results.get(4).getMessage());
		
		test.poll();
		
		assertEquals(5, results.results.size());
		assertEquals("5", results.results.get(4).getMessage());
	}
	
	public void testReAddingListener() {
		
		MockLogPollable root = new MockLogPollable();
		
		this.expected = root;
		
		PollingLogArchiver test = new PollingLogArchiver(
				10, new OurResolver(), new OurEventSource());

		LL results = new LL();
		
		assertEquals(0, results.results.size());
		
		test.poll();
		
		test.addLogListener(results, root, LogLevel.DEBUG, -1, 10);
				
		assertEquals(5, results.results.size());
		assertEquals("5", results.results.get(4).getMessage());
		
		test.removeLogListener(results, root);
		
		test.addLogListener(results, root, LogLevel.DEBUG, 4, 10);
		
		assertEquals(5, results.results.size());
		assertEquals("5", results.results.get(4).getMessage());
		
		test.poll();
		
		assertEquals(5, results.results.size());
		assertEquals("5", results.results.get(4).getMessage());
		
		test.removeLogListener(results, root);
		
	}
}
