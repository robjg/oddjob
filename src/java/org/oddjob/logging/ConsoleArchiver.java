/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

/**
 * A ConsoleArchiver is something which has archived away console messages 
 * for different consoles.
 */
public interface ConsoleArchiver {

	/**
	 * Add a listener to the archiver which will receive all missed
	 * events. The idea of receiving only missed event is to minimise
	 * network traffic when used remotely.
	 * 
	 * @param l The logListener which will recieve the events.
	 * @param component The component whose console to receive events for.
	 * @param last The last event number recieved. The LogArchive will
	 * not send messages from before this number.
	 * @param max The maximum messages to send up to the most recent.
	 */
	public void addConsoleListener(LogListener l, Object component, 
			long last, int max);
	
	/**
	 * Remove the LogListener.
	 * 
	 * @param l The LogListener.
	 */
	public void removeConsoleListener(LogListener l, Object component);
	
	/**
	 * Get the console id for a given component.
	 * 
	 * @param component The component.
	 * @return The console id.
	 */
	public String consoleIdFor(Object component);
		
//	public void onDestroy();
}
