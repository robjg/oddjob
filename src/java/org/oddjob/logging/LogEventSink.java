package org.oddjob.logging;

public interface LogEventSink {

	/**
	 * Add an event to this archive.
	 * 
	 * @param level The level.
	 * @param line The message.
	 */
	public void addEvent(LogLevel level, String line);
	
}
