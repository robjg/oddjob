/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

/**
 * A Listener that is able to listen to log events.
 * 
 * @author Rob Gordon.
 */
public interface LogListener {

	/**
	 * Called when a LogEvent occurs on the thing being
	 * listened to.
	 * 
	 * @param logEvent The LogEvent.
	 */
	public void logEvent(LogEvent logEvent);
}
