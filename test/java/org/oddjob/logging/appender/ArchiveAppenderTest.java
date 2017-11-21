package org.oddjob.logging.appender;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.cache.MockLogArchiverCache;

public class ArchiveAppenderTest extends OjTestCase {

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
	
   @Test
	public void testAppender() {

		OurArchiver archiver = new OurArchiver();
		
		ArchiveAppender test = new ArchiveAppender(
				archiver, LoggerAdapter.layoutFor("%p - %m"));
		
		logger.setLevel(Level.TRACE);
		
		LoggerAdapter.appenderAdapterFor(ArchiveAppenderTest.class).addAppender(test);
		
		logger.trace("trace.");
		logger.debug("debug.");
		logger.info("info.");
		logger.warn("warn.");
		logger.error("error.");
		logger.fatal("fatal.");
		
		LoggerAdapter.appenderAdapterFor(ArchiveAppenderTest.class).removeAppender(test);
		
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
