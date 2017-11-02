/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.server;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;

public class LogArhiverHelperTest extends OjTestCase {
	
	public static class MockLogArchiver implements LogArchiver {
		boolean removed;
		Object component;
		LogLevel level;
		long last;
		int max;
		
		public void addLogListener(LogListener l, Object component, LogLevel level, long last, int max) {
			this.component = component;
			this.level = level;
			this.last = last;
			this.max = max;
			l.logEvent(new LogEvent("org.oddjob.test.LoggingBean", 0, LogLevel.INFO, "Test"));
		}
		public void removeLogListener(LogListener l, Object component) {
			assertEquals(this.component, component);
			removed = true;
		}
		public void onDestroy() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
   @Test
	public void testRetrieveLogEvents() {
		Object bean = new Object();
		
		MockLogArchiver archiver = new MockLogArchiver();
		
		LogEvent[] events = LogArchiverHelper.retrieveLogEvents(bean, archiver, new Long(0), new Integer(10));
		
		assertEquals("component", bean, archiver.component);
		assertEquals("log level", LogLevel.DEBUG, archiver.level);
		assertEquals("from", 0, archiver.last);
		assertEquals("max", 10, archiver.max);
		assertTrue("removed", archiver.removed);
		
		assertEquals("num events", 1, events.length);
		assertEquals("event message", "Test", events[0].getMessage());
	}
}
