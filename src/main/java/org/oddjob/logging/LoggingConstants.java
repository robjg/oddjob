package org.oddjob.logging;

/**
 * Constants for Logging.
 * 
 * @author rob
 *
 */
public interface LoggingConstants {

	/** The MDC for the logger name. */
	public static final String MDC_LOGGER = "ojmdc"; 
	
	/** The MDC for the job name. */
	public static final String MDC_JOB_NAME = "ojname";

	/** Padding for the per job level. */
	public static final String MDC_LEVEL_PADDING = "ojpad";
}
