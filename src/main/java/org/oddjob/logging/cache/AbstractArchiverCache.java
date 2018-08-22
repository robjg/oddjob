/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Stateful;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogListener;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

/**
 * An base implementation of a Cache for LogEvents.
 * <p>
 * @author Rob Gordon
 */
abstract public class AbstractArchiverCache implements LogArchiverCache {
	private static final Logger logger = LoggerFactory.getLogger(AbstractArchiverCache.class);
	
	/** 
	 * Map of archive name to archive. 
	 */
	private final Map<String, LogArchiveImpl> archives = 
		new HashMap<String, LogArchiveImpl>();
	
	/**
	 * Keep track of children and archives so we can delete an archive.
	 */
	private final SimpleCounter counter = new SimpleCounter();
		
	private final int maxHistory;
	
	private final ArchiveNameResolver resolver;
	
	private final StateListener stateListener = new StateListener() {
		
		@Override
		public void jobStateChange(StateEvent event) {
			if (event.getState().isDestroyed()) {
				removeArchive(event.getSource());
				
			}
		}
	};
		
	/**
	 * Default constructor.
	 */
	public AbstractArchiverCache(ArchiveNameResolver resolver) {
		this(resolver, LogArchiver.MAX_HISTORY);
	}
		
	/**
	 * Construct a LogArchiver with the given amount of history.
	 * 
	 * @param maxHistory The number of lines to store for each logger
	 */
	public AbstractArchiverCache(ArchiveNameResolver resolver, int maxHistory) {
		this.resolver = resolver;
		this.maxHistory = maxHistory;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.logging.FunctionalLogArchiver#getMaxHistory()
	 */
	public int getMaxHistory() {
		return maxHistory;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.logging.FunctionalLogArchiver#getLastMessageNumber(java.lang.String)
	 */
	public long getLastMessageNumber(String archive) {
		LogArchive logArchive = archives.get(archive);
		if (logArchive == null) {
			throw new IllegalArgumentException("Archive [" + archive + "] does not exist in this LogArchiver.");
		}
		return logArchive.getLastMessageNumber();
	}

	/**
	 * Add a listener.
	 * 
	 * @param l The listener
	 * @param archive The component
	 * @param level The level
	 * @param last The last message number.
	 * @param history The max messages required.
	 */
	public void addLogListener(LogListener l, Object component,
			LogLevel level, long last, int history) {
		String archive = resolver.resolveName(component);
		LogArchive logArchive = archives.get(archive);
		if (logArchive == null) {
			l.logEvent(LogArchiver.NO_LOG_AVAILABLE);
			return;
		}
		logger.debug("Adding LogListener [" + l + "] for [" 
				+ logArchive.getArchive() + "]");
		logArchive.addListener(l, level, last, history);
	}
		
	/**
	 * Remove a listener.
	 * 
	 * @param l The listener.
	 * @param archive The archive.
	 */
	public void removeLogListener(LogListener l, Object component) {
		String archive = resolver.resolveName(component);
		LogArchive logArchive = archives.get(archive);
		if (logArchive == null) {
			return ;
		}
		logger.debug("Removing LogListener ["
				+ l + "] from [" + logArchive.getArchive() + "]");
		logArchive.removeListener(l);
	}
	
	/**
	 * Does this Archiver contain the given archive.
	 * 
	 * @param archive The archive.
	 * @return true if it does, false if it doesn't.
	 */
	public boolean hasArchive(String archive) {
		return archives.containsKey(archive);
	}

	protected boolean hasArchiveFor(Object component) {
		return hasArchive(resolver.resolveName(component));
	}
	
	protected ArchiveNameResolver getResolver() {
		return resolver;
	}
	
	/**
	 * Add an archive this Log Archiver.
	 * 
	 * @param archive The archive name.
	 */
	protected void addArchive(Object component) {
		
		final String archiveName = resolver.resolveName(component);
		if (archiveName == null) {
			return;
		}
		
		logger.debug("Adding archive [" + archiveName + "] for [" + component + "]");
			
		counter.add(archiveName, new Runnable() {
			public void run() {
				LogArchiveImpl logArchive = new LogArchiveImpl(archiveName, getMaxHistory());
				archives.put(archiveName, logArchive);
				logger.debug("Adding archive for [" + archiveName + "]");
			}
		});
		
		if (component instanceof Stateful) {
			((Stateful) component).addStateListener(stateListener);
		}
	}	

	/**
	 * Remove an archive from this LogArchive.
	 * 
	 * @param archive
	 */
	protected void removeArchive(Object component) {
		final String archiveName = resolver.resolveName(component);
		if (archiveName == null) {
			return;
		}
		
		logger.debug("Removing log archive [" + archiveName + 
				"] for [" + component + "]");
		
		counter.remove(archiveName, new Runnable() {
			public void run() {
				logger.debug("Deleting log archive [" + archiveName + "]");
				archives.remove(archiveName);
			}
		});
	}
	
	/**
	 * Add an event to the cache.
	 * 
	 * @param archive The archive.
	 * @param level The level.
	 * @param message The message.
	 */
	public void addEvent(String archive, LogLevel level, String message) {
		LogArchiveImpl logArchive = archives.get(archive);
		if (logArchive == null) {
			throw new IllegalArgumentException("Archive [" + archive + "] does not exist in this LogArchiver.");
		}
		logArchive.addEvent(level, message);
	}
			
	public abstract void destroy();
}
