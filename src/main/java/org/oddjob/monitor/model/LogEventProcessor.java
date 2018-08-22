/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import org.oddjob.arooa.logging.LogLevel;

/**
 * 
 */
public interface LogEventProcessor {

	public void onClear();
	
	public void onUnavailable();
	
	public void onEvent(String text, LogLevel level);
	
}