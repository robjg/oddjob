package org.oddjob.logging;

public class MockLogArchiver implements LogArchiver {

	public void addLogListener(LogListener l, Object component, LogLevel level,
			long last, int max) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}

	public void onDestroy() {
		throw new RuntimeException("Unexpected from class " + getClass());
	}

	public void removeLogListener(LogListener l, Object component) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
}
