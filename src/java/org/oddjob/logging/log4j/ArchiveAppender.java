package org.oddjob.logging.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.oddjob.logging.LoggingConstants;
import org.oddjob.logging.cache.LogArchiverCache;

/**
 * A Log4j appender which logs to LogArchiver.
 */
public class ArchiveAppender extends AppenderSkeleton
implements LoggingConstants {

	private final LogArchiverCache logArchiver; 
	
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

	public void close() {
	}

	public void append(LoggingEvent event) {
		String archive = event.getLoggerName();
		if (!logArchiver.hasArchive(archive)) {
			archive = (String) event.getMDC(MDC_LOGGER);
			if (!logArchiver.hasArchive(archive)) {
				return;
			}
		}		
		StringBuffer text = new StringBuffer();
		text.append(this.layout.format(event));

		if (layout.ignoresThrowable()) {
			String[] s = event.getThrowableStrRep();
			if (s != null) {
				int len = s.length;
				for (int i = 0; i < len; i++) {
					text.append(s[i]);
					text.append(Layout.LINE_SEP);
				}
			}
		}

		logArchiver.addEvent(archive, Log4jArchiver.convertLevel(event.getLevel()), text.toString());
	}
	
	public boolean requiresLayout() {
		return false;
	}
}
