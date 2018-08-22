/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.logging.cache;

import org.oddjob.logging.LogEvent;

/**
 * A source of log events for a polling archiver.
 * 
 * @author Rob Gordon
 */
public interface LogEventSource {
	
	/**
	 * Retrieve log events.
	 * 
	 * @param component For this component.
	 * @param from From greater than this
	 * @param max up to a maximum.
	 * 
	 * @return Log events.
	 */
	public LogEvent[] retrieveEvents(Object component, long from, int max);
}
