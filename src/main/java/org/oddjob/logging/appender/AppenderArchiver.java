package org.oddjob.logging.appender;

import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogHelper;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.cache.LogArchiverCache;
import org.oddjob.logging.cache.StructuralArchiverCache;

/**
 * A LogArchiver which archives by
 * adding an {@link Appender} to all Components in the hierarchy using their
 * logger property.
 * 
 * @author rob
 */
public class AppenderArchiver implements LogArchiver {

	private final LogArchiverCache logArchiver;

	private final Appender appender;
	
	public AppenderArchiver(Object root, String pattern) {
		logArchiver = new StructuralArchiverCache(root,
				new ArchiveNameResolver() {
					public String resolveName(Object component) {
						return LogHelper.getLogger(component);
					}
			});

		
		appender = new ArchiveAppender(logArchiver, LoggerAdapter.layoutFor(pattern));

		LoggerAdapter.appenderAdapterForRoot().addAppender(appender);
	}
		
	public boolean hasArchive(String archive) {
		return logArchiver.hasArchive(archive);
	}
	
	public void addLogListener(LogListener l, Object component, 
			LogLevel level, long last, int history) {
		logArchiver.addLogListener(l, component, 
				level, last, history);
	}
	
	public void removeLogListener(LogListener l, Object component) {
		logArchiver.removeLogListener(l, component);
	}
	
	public void onDestroy() {
		LoggerAdapter.appenderAdapterForRoot().removeAppender(appender);
		logArchiver.destroy();
	}

}
