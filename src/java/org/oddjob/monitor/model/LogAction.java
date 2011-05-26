/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import org.oddjob.logging.LogLevel;

/**
 *  
 */
abstract public class LogAction {
	
	abstract public void accept(LogEventProcessor processor);
}

class MessageEvent extends LogAction {
	
	private final String text;
	private final LogLevel level;

	public MessageEvent(String text, LogLevel level) {
		this.text = text;
		this.level = level;
	}
	
	public void accept(LogEventProcessor processor) {
		processor.onEvent(text, level);
	}
}

class ClearEvent extends LogAction {
	
	public void accept(LogEventProcessor processor) {
		processor.onClear();
	}
}

class UnavailableEvent extends LogAction {
	public void accept(LogEventProcessor processor) {
		processor.onUnavailable();
	}
}
