/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import org.oddjob.arooa.ArooaConstants;

/**
 * Where Oddjob uses inspection to control/monitor a component these
 * are the properties methods it uses.
 */
public class Reserved {

	/**
	 * Identifies a component.
	 */
	public static final String ID_PROPERTY = ArooaConstants.ID_PROPERTY;
	
	/**
	 * This property is the logger name for use in a
	 * LogArchiver.
	 */
	public static final String TRANSIENT_PROPERTY = "transient";

	/**
	 * This property is the logger name for use in a
	 * LogArchiver.
	 */
	public static final String LOGGER_PROPERTY = "logger";
	
	/**
	 * This property is holds the integer result of a job.
	 */
	public static final String RESULT_PROPERTY = "result";
	
	/**
	 * This property provides a description of the job.
	 */
	public static final String DESCRIBE_METHOD = "describe";
	
	/**
	 * Not implemented yet. The idea that a component can 
	 * contain a more detail and that the monitor can zoon into it.
	 */
	public static final String ZOOM_POINT_PROPERTY = "ojzoompoint";
}
