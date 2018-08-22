/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import org.oddjob.arooa.logging.LogLevel;

/**
 * A LogArchiver is something which has archived away log messages for different
 * components. A components archived log messages are identified by an archive
 * name so that just messages for a component (or archive) can be retrieved.
 */
public interface LogArchiver {

	public static final int MAX_HISTORY = 1000;
	
	public static final LogEvent NO_LOG_AVAILABLE = new LogEvent("SYSTEM", 0, LogLevel.INFO, "No Log available");
	
	/**
	 * Add a listener to the archiver which will receive all missed
	 * events. The idea of receiving only missed events is to minimise
	 * network traffic when used remotely.
	 * <p>
	 * Event numbers begin at 0. To receive all events, last must be -1.
	 * 
	 * @param l The logListener which will receive the events.
	 * @param archive The archive to receive events for.
	 * @param level The level of events required.
	 * @param last The last event number received. The LogArchive will
	 * not send messages from before this number.
	 * @param max The maximum messages to send up to the most recent.
	 */
	public void addLogListener(LogListener l, Object component, 
			LogLevel level, long last, int max);
	
	/**
	 * Remove the LogListener.
	 * 
	 * @param l The LogListener.
	 */
	public void removeLogListener(LogListener l, Object component);	

	
//	public void onDestroy();
}
