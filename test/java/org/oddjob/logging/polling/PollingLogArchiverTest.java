/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.logging.polling;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.cache.LogEventSource;
import org.oddjob.logging.cache.PollingLogArchiver;

public class PollingLogArchiverTest extends OjTestCase {

	private class OurLogEventSource implements LogEventSource {
		Object component;
		long from;
		int max;
		
		public LogEvent[] retrieveEvents(Object component, long from, int max) {
			this.component = component;
			this.from = from;
			this.max = max;
			if (from < 0) {
				return new LogEvent[] {  new LogEvent("org.oddjob.TestLogger", 0, LogLevel.INFO, "Test") };				
			}
			else {
				return new LogEvent[0];
			}
		}
	}

	private class OurArchiveNameResolver implements ArchiveNameResolver {
		Object component;
		public String resolveName(Object component) {
			this.component = component;
			return "org.oddjob.TestLogger";
		}
	}
	
	private class OurLogListener implements LogListener {
		LogEvent e;
		public void logEvent(LogEvent logEvent) {
			this.e = logEvent;
		}
	}
	
   @Test
	public void testPoll() {
		OurLogEventSource source = new OurLogEventSource();
		OurArchiveNameResolver resolver = new OurArchiveNameResolver();
		
		Object component = new Object();
		
		PollingLogArchiver test = new PollingLogArchiver(
				resolver, source);
		
		OurLogListener l = new OurLogListener();
		
		// add a listener because LogPoller only polls what's being listened
		// to.
		test.addLogListener(l, component, LogLevel.INFO, -1, 10);
		
		assertEquals("source component", component, source.component);
		assertEquals("source from", -1, source.from);
		assertEquals("source max", LogArchiver.MAX_HISTORY, source.max);
		
		assertEquals("resolever component", component, resolver.component);
		
		assertNotNull("event", l.e);
		assertEquals("event message", "Test", l.e.getMessage());

		l.e = null;
		
		test.poll();

		assertEquals("source from", 0, source.from);
		assertEquals("source max", LogArchiver.MAX_HISTORY, source.max);

		assertNull("no event", l.e);
	}
}
