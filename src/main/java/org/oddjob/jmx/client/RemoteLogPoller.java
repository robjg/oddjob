/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.*;
import org.oddjob.logging.cache.PollingLogArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LogArchiver which maintains it's log archives by polling a JMX MBean for
 * log information.
 * 
 * @author Rob Gordon
 */
public class RemoteLogPoller 
implements Runnable, LogArchiver, ConsoleArchiver {
	private static final Logger logger = LoggerFactory.getLogger(RemoteLogPoller.class);

	/** Archiver to store console messages retrieved from the server side. */
	private final PollingLogArchiver consoleArchiver;
	
	/** Archiver to store log messages retrieved from the server side. */
	private final PollingLogArchiver loggerArchiver;
	
	/** Interval between polling. */
	private long logPollingInterval = 5000;
	
	/** Stop flag */
	private volatile boolean stop;
	
	/**
	 * Constructor.
	 * 
	 */
	public RemoteLogPoller(Object root, 
			final int consoleHistoryLines, final int logHistoryLines) {
		if (root == null) {
			throw new NullPointerException("Root component must not be null.");
		}
		if (consoleHistoryLines < 1) {
			throw new IllegalArgumentException("Console history lines must be greater than zero.");
		}
		if (logHistoryLines < 1) {
			throw new IllegalArgumentException("Log history lines must be greater than zero.");
		}
		
		consoleArchiver = new PollingLogArchiver(consoleHistoryLines,
				component -> {
					if (component instanceof LogPollable) {
						return consoleArchiveFor((LogPollable) component);
					}
					else {
						return null;
					}
				},
				(component, last, max) -> {
					logger.debug("Retrieving console events for [" + component + "]");
					LogPollable pollable = (LogPollable) component;
					return pollable.retrieveConsoleEvents(last, max);
				}
		);
		
		loggerArchiver = new PollingLogArchiver(logHistoryLines,
				component -> {
					if (component instanceof LogPollable) {
						return logArchiveFor((LogPollable) component);
					}
					else {
						return null;
					}
				},
				(component, last, max) -> {
					LogPollable pollable = (LogPollable) component;
					LogEvent[] results = pollable.retrieveLogEvents(last, max);
					logger.debug("Retrieved [" + results.length + "] log events for [" + component + "]");
					return results;
				}
		);
		
	}

	/**
	 * Utility function to get the log archive name.
	 * 
	 * @return The log archive name for the given component.
	 */
	static String logArchiveFor(LogPollable component) {
		String url = component.url();
		if (url == null) {
			throw new NullPointerException("[" + component + "] has no URL.");
		}
		String archive = LogHelper.getLogger(component);
		if (archive == null) {
			// Will become No Log Available.
			return null;
		}						
		return  url + "#" + archive;		
	}
	
	/**
	 * Utility function to get the console archive name.
	 * 
	 * @return The console archive name for the given component.
	 */
	static String consoleArchiveFor(LogPollable component) {
		String url = component.url();
		if (url == null) {
			throw new NullPointerException("Proxy for [" + component + "] has no URL.");
		}
		String consoleId = component.consoleId();
		if (consoleId == null) {
			throw new NullPointerException("Proxy for [" + component + "] has no consoleId.");
		}						
		return url + "#" + consoleId;		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.logging.LogArchiver#addLogListener(org.oddjob.logging.LogListener, java.lang.Object, org.oddjob.logging.LogLevel, long, int)
	 */
	public void addLogListener(LogListener l, Object component,
			LogLevel level, long last, int max) {
		loggerArchiver.addLogListener(l, component, level, last, max);
		synchronized (this) {
			// Force a poll.
			notifyAll();
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.logging.LogArchiver#removeLogListener(org.oddjob.logging.LogListener)
	 */
	public void removeLogListener(LogListener l, Object component) {
		loggerArchiver.removeLogListener(l, component);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#addConsoleListener(org.oddjob.logging.LogListener, java.lang.Object, long, int)
	 */
	public void addConsoleListener(LogListener l, Object component, long last,
			int max) {
		consoleArchiver.addLogListener(l, component, LogLevel.DEBUG, last, max);
		synchronized (this) {
			// Force a poll.
			notifyAll();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#removeConsoleListener(org.oddjob.logging.LogListener, java.lang.Object)
	 */
	public void removeConsoleListener(LogListener l, Object component) {
		consoleArchiver.removeLogListener(l, component);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#consoleIdFor(java.lang.Object)
	 */
	public String consoleIdFor(Object component) {
		return consoleArchiveFor((LogPollable) component);
	}
	
	public long getLogPollingInterval() {
		return logPollingInterval;
	}
	
	public void setLogPollingInterval(long logPollingInterval) {
		if (logPollingInterval == 0) {
			throw new IllegalArgumentException("Log Polling Interval must be greater than zero.");
		}
		this.logPollingInterval = logPollingInterval;
	}
	
	/**
	 * Poll a remote MBean for Log Messages.
	 *
	 */
	public void poll() {
		consoleArchiver.poll();
		loggerArchiver.poll();
	}

	public void run() {
		while (!stop) {
			poll();
			synchronized (this) {	
				try {		
					wait(logPollingInterval);
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}
		consoleArchiver.onDestroy();
		loggerArchiver.onDestroy();		
	}

	/**
	 * Stop polling.
	 */
	public void stop() {
		stop = true;
		synchronized (this) {
			notifyAll();
		}
	}

	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
}
