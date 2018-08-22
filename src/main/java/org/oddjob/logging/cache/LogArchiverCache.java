package org.oddjob.logging.cache;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogListener;

/**
 * Maintains a collection of {@link LogArchive}s by archive name.
 * 
 * @author rob
 *
 */
public interface LogArchiverCache {

	/**
	 * Does this Acchiver contain the given archive.
	 * 
	 * @param archive The archive.
	 * @return true if it does, false if it doesn't.
	 */
	public boolean hasArchive(String archive);
		
	
	public long getLastMessageNumber(String archive);
	
	/**
	 * Add a listener.
	 * 
	 * @param l The listener
	 * @param archive The archive
	 * @param level The level
	 * @param last The last message number.
	 * @param history The max messages required.
	 */
	public void addLogListener(LogListener l, Object component,
			LogLevel level, long last, int history);
		
	/**
	 * Remove a listener.
	 * 
	 * @param l The listener.
	 * @param archive The archive.
	 */
	public void removeLogListener(LogListener l, Object component);
	

	public int getMaxHistory();
	
	/**
	 * Add an event to the cache.
	 * 
	 * @param archive The archive.
	 * @param level The level.
	 * @param message The message.
	 */
	public void addEvent(String archive, LogLevel level, String message);
	
	public abstract void destroy();
}
