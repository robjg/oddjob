/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

import org.oddjob.arooa.logging.LogLevel;

/**
 * A log archive. This archives events and supports listeners.
 * 
 * @author Rob Gordon
 */
public interface LogArchive {
	
	/**
	 * Get the last message number in this archive.
	 * 
	 * @return The last message number.
	 */
	public long getLastMessageNumber();

	/**
	 * Retrieve events from the archive. The most recent events are retrieved
	 * first.
	 * 
	 * @param from From message number
	 * @param max The maximum number to retreive.
	 * 
	 * @return The events.
	 */
	public LogEvent[] retrieveEvents(long from, int max);
	
	/**
	 * Add a listener.
	 * 
	 * @param logListener The listener.
	 * @param level The level.
	 * @param last The last message number this listener requires.
	 * @param history The maximum lines this listener requires.
	 */
	public void addListener(LogListener logListener,  
			LogLevel level, long last, int history);
	
	/**
	 * Remove a listener.
	 * 
	 * @param l The listener.
	 */
	public boolean removeListener(LogListener l);

	/**
	 * Get the archive name.
	 * 
	 * @return The archive name. Never null.
	 */
	public String getArchive();
	
	/**
	 * Get the maximum number archive history lines supported.
	 * 
	 * @return The number of lines.
	 */
	public int getMaxHistory();

}
