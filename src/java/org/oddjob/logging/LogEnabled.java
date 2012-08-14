/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging;

/**
 * A component that has it's own logger. This is an alternative
 * to a component providing a logger property because DynaBeans can 
 * make providing that property difficult.
 *
 */
public interface LogEnabled {
	
	/**
	 * Provide the logger name. The name should be unique for each
	 * component that is LogEnabled.
	 * 
	 * @return A logger name. Never Null.
	 */
	public String loggerName();
}
