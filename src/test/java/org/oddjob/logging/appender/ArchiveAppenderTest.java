package org.oddjob.logging.appender;

import org.junit.Test;
import org.oddjob.arooa.logging.AppenderAdapter;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.logging.cache.LogArchiverCache;
import org.oddjob.logging.cache.MockLogArchiverCache;
import org.oddjob.util.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ArchiveAppenderTest {

	private static final Logger logger = 
		LoggerFactory.getLogger(ArchiveAppenderTest.class);
	
	private static class OurArchiver extends MockLogArchiverCache {

		Map<LogLevel, String> messages = new HashMap<>();
		
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
	public void testAppenderCapturesMessagesToLogger() {

		OurArchiver archiver = new OurArchiver();
		
		ArchiveAppender test = new ArchiveAppender(
				archiver);
		
		
		AppenderAdapter appenderAdapter = LoggerAdapter.appenderAdapterFor(ArchiveAppenderTest.class);
		appenderAdapter.setLevel(LogLevel.TRACE);
		appenderAdapter.addAppender(test, LoggerAdapter.layoutFor("%p - %m"));
		
		logger.trace("trace.");
		logger.debug("debug.");
		logger.info("info.");
		logger.warn("warn.");
		logger.error("error.");
		
		appenderAdapter.removeAppender(test);
		
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
	}
    
    @Test
	public void testAppenderCapturesMessagesToNestedLoggerOnSeparateThread() throws InterruptedException {

    	Logger nestedLogger = LoggerFactory.getLogger(ArchiveAppenderTest.class.getName() + ".Nested");
    	
    	
    	LogArchiverCache archiver = mock(LogArchiverCache.class);
    	when(archiver.hasArchive(logger.getName())).thenReturn(true);
    	
		ArchiveAppender test = new ArchiveAppender(archiver);
		
		
		AppenderAdapter appenderAdapter = LoggerAdapter.appenderAdapterFor(nestedLogger.getName());
		appenderAdapter.addAppender(test, LoggerAdapter.layoutFor("%m"));
		
		try (Restore restore = OddjobNDC.push(logger.getName(), "Our Job")) {
			
			nestedLogger.info("Same Thread");
				
			Thread t = new Thread(() -> nestedLogger.info("New Thread"));
			
			t.start();
			t.join();	
		}

		verify(archiver).addEvent(eq(logger.getName()), eq(LogLevel.INFO),
				eq("Same Thread"));
		verify(archiver).addEvent(eq(logger.getName()), eq(LogLevel.INFO),
				eq("New Thread"));
		
		appenderAdapter.removeAppender(test);
		
	}
    
}
