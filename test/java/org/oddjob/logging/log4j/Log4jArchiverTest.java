/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.log4j;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.oddjob.Structural;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
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
		Log4jArchiver archiver = new Log4jArchiver(x, "%m");
		
		Logger logger = Logger.getLogger("foo");
		logger.setLevel(Level.DEBUG);
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
		
		Log4jArchiver archiver = new Log4jArchiver(root, "%m");
		
		Logger logger = Logger.getLogger("foo");
		logger.setLevel(Level.DEBUG);
		logger.debug("Hello World");

		TestListener tl = new TestListener();
		archiver.addLogListener(tl, x, LogLevel.DEBUG, -1, 2000);
		
		assertEquals("event message", "Hello World", tl.le.getMessage());
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
		
		Log4jArchiver archiver = new Log4jArchiver(root, "%m");
		
		Logger logger = Logger.getLogger("foo");
		logger.setLevel(Level.DEBUG);
		logger.debug("Hello World");

		TestListener tl = new TestListener();
		archiver.addLogListener(tl, x, LogLevel.DEBUG, -1, 2000);
		
		assertEquals("event message", "Hello World", tl.le.getMessage());
	}
}
