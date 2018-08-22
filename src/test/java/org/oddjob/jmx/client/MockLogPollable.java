package org.oddjob.jmx.client;

import org.oddjob.logging.LogEvent;

public class MockLogPollable implements LogPollable {

	public String consoleId() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public LogEvent[] retrieveConsoleEvents(long from, int max) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public LogEvent[] retrieveLogEvents(long from, int max) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public String url() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
