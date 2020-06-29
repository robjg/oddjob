/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for return log and console messages from an
 * OddjobMBean.
 */
public class LogArchiverHelper {

	/**
	 * Listener used to gather local events.
	 *
	 */
	static class LL implements LogListener {
		final List<LogEvent> events = new ArrayList<>();
		public void logEvent(LogEvent logEvent) {
			events.add(logEvent);
		}
	}
	
	/**
	 * Get an array of the latest log events.
	 * 
	 * @param component The componet to get log events for.
	 * @param archiver The LogArchiver.
	 * @param last The sequence of the last event required. 
	 * @param max The total number of events.
	 * 
	 * @return An array of LogEvent objects.
	 */
	public static LogEvent[] retrieveLogEvents(Object component, 
			LogArchiver archiver, Long last, Integer max) {		
		if (archiver == null) {
			throw new NullPointerException("No LogArchiver availble on server.");
		}
		LL ll = new LL();
		archiver.addLogListener(ll, component, LogLevel.DEBUG,
				last, max);
		archiver.removeLogListener(ll, component);
		
		return ll.events.toArray(new LogEvent[0]);
	}
	
	/**
	 * Get the console id.
	 * 
	 * @param component The component.
	 * @param archiver The ConsoleArchvier.
	 * 
	 * @return The console archiver id.
	 */
	public static String consoleId(Object component, 
			ConsoleArchiver archiver) {
		String consoleId = archiver.consoleIdFor(component);
		if (consoleId == null) {
			throw new NullPointerException("No console id for [" + component + "]");
		}
		return consoleId;
	}
	
	/**
	 * Get an array of the latest console events.
	 * 
	 * @param component The component.
	 * @param archiver The ConsoleArchvier.
	 * @param last The last event number.
	 * @param max The maximum events required.
	 * 
	 * @return An array of LogEvent objects.
	 */
	public static LogEvent[] retrieveConsoleEvents(Object component, 
			ConsoleArchiver archiver, Long last, Integer max) {
		LL ll = new LL();
		if (archiver == null) {
			throw new NullPointerException("No ConsoleArchiver availble on server.");
		}
		archiver.addConsoleListener(ll, component,
				last, max);
		archiver.removeConsoleListener(ll, component);
		
		return ll.events.toArray(new LogEvent[0]);
	}
	
}
