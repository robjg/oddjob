package org.oddjob.logging;

import org.oddjob.arooa.logging.LogLevel;

/**
 * Something that is able to accept log messages.
 * 
 * @author rob
 *
 */
public interface LogEventSink {

	/**
	 * Add an event to this archive.
	 * 
	 * @param level The level.
	 * @param line The message.
	 */
	public void addEvent(LogLevel level, String line);
	
}
