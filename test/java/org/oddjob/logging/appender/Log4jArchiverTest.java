/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.appender;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.Structural;
import org.oddjob.arooa.logging.AppenderAdapter;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.MockLogArchiver;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * 
 */
public class Log4jArchiverTest extends OjTestCase {
	
	private class X implements LogEnabled {
		public String loggerName() {
			return "foo";
		}
	}	

	private class TestListener implements LogListener {
		LogEvent le;
		public void logEvent(LogEvent logEvent) {
			le = logEvent;
		}
	}
	
	// test Log4jArchiver archives a message sent to a Log4j Logger.
   @Test
	public void testSimpleLogOutputCaptured() {
		X x = new X();
		AppenderArchiver archiver = new AppenderArchiver(x, "%m");
		
		AppenderAdapter adapter = LoggerAdapter.appenderAdapterFor("foo");
		adapter.setLevel(LogLevel.DEBUG);

		Logger logger = Logger.getLogger("foo");
		logger.debug("Hello World");

		TestListener tl = new TestListener();
		archiver.addLogListener(tl, x, LogLevel.DEBUG, -1, 2000);
		
		assertEquals("event message", "Hello World", tl.le.getMessage());
	}
	
	private class OurStructural implements Structural {

		ChildHelper<Object> children = new ChildHelper<Object>(this);
		
		@Override
		public void addStructuralListener(StructuralListener listener) {
			children.addStructuralListener(listener);
		}
		
		@Override
		public void removeStructuralListener(StructuralListener listener) {
			children.removeStructuralListener(listener);
		}
	}
	
   @Test
	public void testChildLogOutputCaptured() {
		
		X x = new X();
		
		OurStructural root = new OurStructural();
		root.children.insertChild(0, x);
		
		AppenderArchiver archiver = new AppenderArchiver(root, "%m");

		AppenderAdapter adapter = LoggerAdapter.appenderAdapterFor("foo")
				.setLevel(LogLevel.DEBUG);
		
		Logger logger = Logger.getLogger("foo");
		logger.debug("Hello World");

		TestListener tl = new TestListener();
		archiver.addLogListener(tl, x, LogLevel.DEBUG, -1, 2000);
		
		assertEquals("event message", "Hello World", tl.le.getMessage());

		adapter.setLevel(LogLevel.INFO);
	}
	
	private class OurArchiver extends MockLogArchiver implements LogEnabled {
		
		public String loggerName() {
			return "foo";
		}
	}
	
   @Test
	public void testLogArchiverChildLogOutputCaptured() {
		
		OurArchiver x = new OurArchiver();
		
		OurStructural root = new OurStructural();
		root.children.insertChild(0, x);
		
		AppenderArchiver archiver = new AppenderArchiver(root, "%m");

		AppenderAdapter adapter = LoggerAdapter.appenderAdapterFor("foo")
				.setLevel(LogLevel.DEBUG);
		
		Logger logger = Logger.getLogger("foo");
		logger.debug("Hello World");

		TestListener tl = new TestListener();
		archiver.addLogListener(tl, x, LogLevel.DEBUG, -1, 2000);
		
		assertEquals("event message", "Hello World", tl.le.getMessage());
		
		adapter.setLevel(LogLevel.INFO);
	}
}
