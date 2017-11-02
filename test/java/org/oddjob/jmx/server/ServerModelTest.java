/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.server;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.oddjob.arooa.registry.MockBeanRegistry;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.util.MockThreadManager;

public class ServerModelTest extends OjTestCase {

	class LL implements LogListener {
		LogEvent e;
		public void logEvent(LogEvent logEvent) {
			this.e = logEvent;
		}
	}
	
	public static class LoggingBean implements LogEnabled {
		public String loggerName() {
			return "org.oddjob.test.LoggingBean";
		}
	}
	
	class OurRegistry extends MockBeanRegistry {
		@Override
		public String getIdFor(Object component) {
			return "apple";
		}
	}
	
	/**
	 * Test retrieving log events
	 *
	 */
   @Test
	public void testLogArchiver() throws Exception {
		LoggingBean bean = new LoggingBean();
		
		ServerModelImpl sm = new ServerModelImpl(
				new ServerId("//test"), 
				new MockThreadManager(), 
				new MockServerInterfaceManagerFactory());
		
		sm.setLogFormat("%m");
							
		Logger testLogger = Logger.getLogger(bean.loggerName());
		
		LL ll = new LL();

		ServerContext serverContext = new ServerContextImpl(
				bean, sm, new OurRegistry());
		
		testLogger.setLevel(Level.DEBUG);
		testLogger.info("Test");
		
		serverContext.getLogArchiver().addLogListener(ll, bean, LogLevel.DEBUG, -1, 10);
		
		assertNotNull("event", ll.e);
		assertEquals("event message", "Test", ll.e.getMessage());
	}
}
