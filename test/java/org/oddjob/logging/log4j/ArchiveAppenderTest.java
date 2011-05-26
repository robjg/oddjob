package org.oddjob.logging.log4j;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.cache.MockLogArchiverCache;

public class ArchiveAppenderTest extends TestCase {

	private static final Logger logger = 
		Logger.getLogger(ArchiveAppenderTest.class);
	
	private class OurArchiver extends MockLogArchiverCache {

		Map<LogLevel, String> messages = new HashMap<LogLevel,String>();
		
		@Override
		public void addEvent(String archive, LogLevel level, String message) {
			assertEquals(ArchiveAppenderTest.class.getName(), archive);
			messages.put(level, message);
		}
		
		@Override
		public boolean hasArchive(String archive) {
			assertEquals(ArchiveAppenderTest.class.getName(), archive);
			return true;
		}
	}
	
	public void testAppender() {

		OurArchiver archiver = new OurArchiver();
		
		ArchiveAppender test = new ArchiveAppender(
				archiver, new SimpleLayout());
		
		logger.setLevel(Level.TRACE);
		
		logger.addAppender(test);
		
		logger.trace("trace.");
		logger.debug("debug.");
		logger.info("info.");
		logger.warn("warn.");
		logger.error("error.");
		logger.fatal("fatal.");
		
		logger.removeAppender(test);
		
		assertEquals("TRACE - trace.", 
				archiver.messages.get(LogLevel.TRACE).trim());
		assertEquals("DEBUG - debug.", 
				archiver.messages.get(LogLevel.DEBUG).trim());
		assertEquals("INFO - info.", 
				archiver.messages.get(LogLevel.INFO).trim());
		assertEquals("WARN - warn.", 
				archiver.messages.get(LogLevel.WARN).trim());
		assertEquals("ERROR - error.", 
				archiver.messages.get(LogLevel.ERROR).trim());
		assertEquals("FATAL - fatal.", 
				archiver.messages.get(LogLevel.FATAL).trim());
	}
}
