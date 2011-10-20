/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogHelper;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.cache.LogArchiverCache;
import org.oddjob.logging.cache.StructuralArchiverCache;

/**
 * A LogArchiver which archives using Log4j. This Archiver works by
 * adding a Log4j appender to all Components in the hierarchy using their
 * logger property.
 */
public class Log4jArchiver implements LogArchiver {

	private final LogArchiverCache logArchiver;

	private final Appender appender;
	
	public Log4jArchiver(Object root, String pattern) {
		logArchiver = new StructuralArchiverCache(root,
				new ArchiveNameResolver() {
					public String resolveName(Object component) {
						return LogHelper.getLogger(component);
					}
			});
		appender = new ArchiveAppender(logArchiver, new PatternLayout(pattern));
		appender.setName(this.toString());
		Logger.getRootLogger().addAppender(appender);
	}
		
	public boolean hasArchive(String archive) {
		return logArchiver.hasArchive(archive);
	}
	
	public void addLogListener(LogListener l, Object component, 
			LogLevel level, long last, int history) {
		logArchiver.addLogListener(l, component, 
				level, last, history);
	}
	
	public void removeLogListener(LogListener l, Object component) {
		logArchiver.removeLogListener(l, component);
	}
	
	public void onDestroy() {
		Logger.getRootLogger().removeAppender(appender);
		logArchiver.destroy();
	}

	/**
	 * Utility function to convert Log4j log level.
	 * @param level The log4j level.
	 * @return The Oddjob level.
	 */
	@SuppressWarnings("deprecation")
	public static LogLevel convertLevel(Level level) {
		if (level == Level.ALL) {
			return LogLevel.TRACE;
		}
		else if (level == Level.TRACE) {
			return LogLevel.TRACE;
		}
		else if (level == Level.DEBUG || level == Priority.DEBUG) {
			return LogLevel.DEBUG;
		}
		else if (level == Level.INFO || level == Priority.INFO) {
			return LogLevel.INFO;
		}
		else if (level == Level.WARN || level == Priority.WARN) {
			return LogLevel.WARN;
		}
		else if (level == Level.ERROR || level == Priority.ERROR) {
			return LogLevel.ERROR;
		}
		else if (level == Level.FATAL || level == Priority.FATAL) {
			return LogLevel.FATAL;
		}
		else if (level == Level.OFF) {
			return LogLevel.FATAL;
		}
		else {
			throw new IllegalArgumentException("Don't know anything about [" + level + "]");
		}
	}
}
