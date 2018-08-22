package org.oddjob.logging.appender;

import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.Layout;
import org.oddjob.arooa.logging.LoggingEvent;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.logging.cache.LogArchiverCache;

/**
 * The {@link Appender} which logs to the {@link AppenderArchiver}.
 * 
 * @author rob
 */
public class ArchiveAppender implements Appender {

	private final LogArchiverCache logArchiver; 
	
	private final Layout layout;
	
	/**
	 * Create an ArchiveAppender using the given logArchiver and layout.
	 * 
	 * @param logArchiver The LogArchiver to archive to.
	 * @param layout The layout to use.
	 */
	public ArchiveAppender(LogArchiverCache logArchiver, Layout layout) {
		this.logArchiver = logArchiver;
		this.layout = layout;
	}

	@Override
	public void append(LoggingEvent event) {
		String archive = event.getLoggerName();
		if (!logArchiver.hasArchive(archive)) {
			archive = OddjobNDC.current()
					.map(lc -> lc.getLogger())
					.orElse(null);
			if (!logArchiver.hasArchive(archive)) {
				return;
			}
		}		
		
		logArchiver.addEvent(archive, event.getLevel(), this.layout.format(event));
	}	
}
