/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogListener;

/**
 * A Cache for Log Events. This cache tracks changes to the structure of
 * the component tree and adds and removes LogArchives.
 * <p>
 * 
 * @author Rob Gordon.
 */
public class LazyArchiverCache extends AbstractArchiverCache {
	/**
	 * Construct a LogArchiver archiving message from the given root object
	 * and all it's children.
	 * 
	 * @param root The root object.
	 * @param resolver A reslover which resolves archive name, either locally
	 * or using a remote url.
	 */
	public LazyArchiverCache(ArchiveNameResolver resolver) {
		this(LogArchiver.MAX_HISTORY, resolver);
	}
		
	/**
	 * Construct a LogArchiver archiving message from the given root object
	 * and all it's children with the given amount of history.
	 * 
	 * @param root The root object.
	 * @param maxHistory The number of lines to store for each logger
	 * @param resolver A reslover which resolves archive name, either locally
	 * or using a remote url.
	 */
	public LazyArchiverCache(int maxHistory, ArchiveNameResolver resolver) {
		super(resolver, maxHistory);
	}

	@Override
	synchronized public void addLogListener(LogListener l, Object component, LogLevel level,
			long last, int history) {
		if (!hasArchiveFor(component)) {
			addArchive(component);
		}
		super.addLogListener(l, component, level, last, history);
	}
	
	@Override
	synchronized public void removeLogListener(LogListener l, Object component) {
		super.removeLogListener(l, component);
	}
	
	public void destroy() {
	}
}
