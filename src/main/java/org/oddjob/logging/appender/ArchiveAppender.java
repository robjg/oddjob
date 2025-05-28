package org.oddjob.logging.appender;

import org.oddjob.arooa.logging.Appender;
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

    /**
	 * Create an ArchiveAppender using the given logArchiver and layout.
	 * 
	 * @param logArchiver The LogArchiver to archive to.
	 */
	public ArchiveAppender(LogArchiverCache logArchiver) {
		this.logArchiver = logArchiver;
    }

	@Override
	public void append(LoggingEvent event) {
		String archive = event.getLoggerName();
		if (!logArchiver.hasArchive(archive)) {
			archive = OddjobNDC.current()
					.map(OddjobNDC.LogContext::getLogger)
					.orElse(null);
			if (!logArchiver.hasArchive(archive)) {
				return;
			}
		}		
		
		logArchiver.addEvent(archive, event.getLevel(), event.getMessage());
	}	
}
