package org.oddjob.logging;

public class MockConsoleArchiver implements ConsoleArchiver {

	public void addConsoleListener(LogListener l, Object component, long last,
			int max) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public void removeConsoleListener(LogListener l, Object component) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public String consoleIdFor(Object component) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public void onDestroy() {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
}
