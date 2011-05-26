/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogEventSink;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;

/**
 * A log archive. This archives events and supports listeners.
 * 
 * @author Rob Gordon
 */
public class LogArchiveImpl implements LogArchive, LogEventSink {
	
	/** Maximum archived lines */
	private final int maxHistory;

	private final String archive;
	
	/** Archives are stored as a linked list with new message at the beginning
	 * (first) and old messages at the end (last).
	 */
	private final LinkedList<LogEvent> events = new LinkedList<LogEvent>();
	
	/** Map of listeners to level. */
	private final Map<LogListener, LogLevel> listeners = 
		new HashMap<LogListener, LogLevel>();
	
	/**
	 * Constructor.
	 * 
	 * @param maxHistory The maximum history lines.
	 */
	public LogArchiveImpl(String archive, int maxHistory) {
		if (archive == null) {
			throw new NullPointerException("Logger Name must not be null.");
		}
		this.archive = archive;
		this.maxHistory = maxHistory;
	}
	
	/**
	 * Get the last message number in this archive.
	 * 
	 * @return The last message number.
	 */
	public long getLastMessageNumber() {
		synchronized (events) {
			if (events.size() == 0) {
				return -1;
			}
			LogEvent logEvent = (LogEvent) events.getFirst();
			return logEvent.getNumber();
		}
	}

	/**
	 * Add an event to this archive.
	 * 
	 * @param level The level.
	 * @param line The message.
	 */
	public void addEvent(LogLevel level, String line) {
		synchronized (events) {
			LogEvent event = new LogEvent(archive, getLastMessageNumber() + 1, level, 
					line);
			events.addFirst(event);
			while (events.size() > maxHistory) {
				events.removeLast();
			}
			// send event to listeners
			for (Map.Entry<LogListener, LogLevel> entry : listeners.entrySet()) {
				LogListener listener = (LogListener) entry.getKey();
				LogLevel listenerLevel = (LogLevel) entry.getValue();
				if (level.isLessThan(listenerLevel)) {
					continue;
				}
				listener.logEvent(event);
			}
		}
	}
	
	/**
	 * Retrieve events from the archive. The most recent events are retrieved
	 * first.
	 * 
	 * @param from From message number
	 * @param max The maximum number to retreive.
	 * 
	 * @return The events.
	 */
	public LogEvent[] retieveEvents(long from, int max) {
		synchronized (events) {
			List<LogEvent> missed = new ArrayList<LogEvent>();
			int count = 0;
			// work out what has been missed.
			for (Iterator<LogEvent> it = events.iterator(); it.hasNext() && count < max; count++) {
				LogEvent event = it.next();
				if (event.getNumber() == from) {
					break;
				}
				missed.add(event);
			}
			Collections.reverse(missed);
			return (LogEvent[]) missed.toArray(new LogEvent[0]);
		}
	}
	
	/**
	 * Add a listener.
	 * 
	 * @param l The listener.
	 * @param level The level.
	 * @param last The last message number this listener requires.
	 * @param history The maximum lines this listener requires.
	 */
	public void addListener(LogListener l,  
			LogLevel level, long last, int history) {
		synchronized (events) {
			Stack<LogEvent> missed = new Stack<LogEvent>();
			int count = 0;
			// work out what messages listener has missed.
			for (Iterator<LogEvent> it = events.iterator(); it.hasNext() && count < history; count++) {
				LogEvent event = it.next();
				if (event.getNumber() <= last) {
					break;
				}
				if (event.getLevel().isLessThan(level)) {
					continue;
				}
				missed.push(event);
			}
			// send missed messages
			while (!missed.empty()) {
				LogEvent event = (LogEvent) missed.pop();
				l.logEvent(event);
			}
			listeners.put(l, level);
		}
	}

	/**
	 * Remove a listener.
	 * 
	 * @param l The listener.
	 */
	public boolean removeListener(LogListener l) {
		synchronized (events) {
			return !(listeners.remove(l) == null);
		}
	}

	/**
	 * Get the archive name.
	 * 
	 * @return The archive name.
	 */
	public String getArchive() {
		return archive;
	}
	
	/**
	 * Get the naximum number archive history lines supported.
	 * 
	 * @return The number of lines.
	 */
	public int getMaxHistory() {
		return maxHistory;
	}

}
