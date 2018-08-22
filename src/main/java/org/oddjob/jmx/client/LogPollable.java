package org.oddjob.jmx.client;

import org.oddjob.logging.LogEvent;

/**
 * An interface for a client side proxy to implement so that a
 * remote object can be polled for log messages.
 * <p>
 * The idea of being able to poll is that a remote component
 * might be generating thousands of log message which would
 * flood the network if they arrived as notifications. Although
 * polling would miss messages it maintains performance and
 * the time you really want to see messages is when the remote
 * object crashes or hangs.
 * <p>
 * Polling provides an indication that a remote component is
 * working or what a potential problem might be.
 * 
 * @author Rob Gordon
 */
public interface LogPollable {

	/**
	 * Get the url for the remote server which we can use to create a unique
	 * logger name.
	 * 
	 * @return
	 */
	public String url();
	
	/**
	 * Get the consoleId. The consoleId identifies the console on 
	 * a remote server. The console will frequently be
	 * shared between components in a single JVM and so we don't want to get the same
	 * messages on a component by component bases.
	 *  
	 * @return The consoleId.
	 */
	public String consoleId();
	
	/**
	 * Poll for LogEvents.
	 * 
	 * @param from
	 * @param max
	 * @return
	 */
	public LogEvent[] retrieveLogEvents(long from, int max);
	
	/**
	 * Poll for console events.
	 * 
	 * @param from
	 * @param max
	 * @return
	 */
	public LogEvent[] retrieveConsoleEvents(long from, int max);

}
