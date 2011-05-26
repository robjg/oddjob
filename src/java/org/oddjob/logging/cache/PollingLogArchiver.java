/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;

/**
 * A LogArchiver which maintains it's log archives by polling.
 * <p>
 * This archiver will remove a component from it's list of components to poll
 * when no more listeners are listening to it.
 * <p>
 * This archiver will only poll the first component where many components share
 * the same archive.
 * 
 * @author Rob Gordon
 */
public class PollingLogArchiver implements LogArchiver {
	private static final Logger logger = Logger.getLogger(PollingLogArchiver.class);
	
	/** A local LogArchiver we delegate to. */
	private final LogArchiverCache cache;
	
	/** Components we want events for and there id */
	private final Map<Object, String> components = 
		new HashMap<Object, String>();
	
	/** last message numbers */
	private final Map<String, Long> lastMessageNumbers = 
		new HashMap<String, Long>();
	
	/** How many listeners are listening to a component. */
	private SimpleCounter listenerCounter = new SimpleCounter();

	private final LogEventSource source;
	
	private final ArchiveNameResolver resolver;
	
	/**
	 * Constructor with default history.
	 * 
	 */
	public PollingLogArchiver( 
			ArchiveNameResolver resolver, LogEventSource source) {
		this(LogArchiver.MAX_HISTORY, resolver, source);
	}

	/**
	 * Constructor that accepts a history count.
	 * 
	 */
	public PollingLogArchiver(int history,
			ArchiveNameResolver resolver, LogEventSource source) {
		this.source = source;
		this.resolver = resolver;
		this.cache = new LazyArchiverCache(history, resolver);
	}
	
	/**
	 * Add a LogListener for the given component.
	 * 
	 * @param l The LogListener.
	 * @param component The component.
	 * @param level The level.
	 * @param last The last message number required.
	 * @param max The maximum history.
	 */
	public void addLogListener(LogListener l, Object component,
			LogLevel level, long last, int max) {
		String archive = resolver.resolveName(component);
		logger.debug("Adding LogListener for [" + archive + "]");
		if (archive == null) {
			l.logEvent(LogArchiver.NO_LOG_AVAILABLE);
			return;
		}
		synchronized (components) {
			components.put(component, archive);
			listenerCounter.add(component);
			cache.addLogListener(l, component, 
					level, last, max);
			poll();
		}
	}
	
	/**
	 * Remove the LogListener for the given component.
	 *  
	 * @param l The LogListener.
	 * @param component The component.
	 */
	public void removeLogListener(LogListener l, final Object component) {
		synchronized (components) {
			final String archiveName = (String) components.get(component);
			if (archiveName == null) {
				return;
			}
			cache.removeLogListener(l, component);
			// if the cache had the listener then decrease the count of
			// things for that component. The runnable runs within 
			// the synchronized method call ensuring serialized removal
			// from the components.
			listenerCounter.remove(component, new Runnable() {
				public void run() {
					components.remove(component);
				}
			});
		}
	}
	
	/**
	 * Poll for Log Messages.
	 *
	 */
	public void poll() {
		synchronized (components) {
			Set<Object> polled = new HashSet<Object>();
			Set<Map.Entry<Object, String>> copy = 
				new HashSet<Map.Entry<Object,String>>(components.entrySet());
			for (Map.Entry<Object, String> entry : copy) {
				
				Object component = entry.getKey();
				String archiveName = entry.getValue();
				// stops polling the same archive when components share the same
				// logger/console
				if (polled.contains(archiveName)) {
					continue;
				}

				Long lastMessageNumber = lastMessageNumbers.get(archiveName);
				if (lastMessageNumber == null) {
					lastMessageNumber = -1L;
				}
				
				LogEvent[] events = null;
				
				try {
					// this could fail if the remote node has gone or the connection
					// has dropped.
					events = source.retrieveEvents(component, 
							lastMessageNumber,
							cache.getMaxHistory());
				} catch (Exception e) {
					logger.debug("Failed to retrieve events for [" + component + "]", e);
					components.remove(component);
					continue;
				}
				for (int i = 0; i < events.length; ++i) {
					cache.addEvent(archiveName, events[i].getLevel(), events[i].getMessage());
				}
				if (events.length > 0) {
					lastMessageNumbers.put(archiveName, events[events.length - 1].getNumber());
				}
				polled.add(archiveName);
			}
		}
	}

	public void onDestroy() {
		cache.destroy();
	}
}
