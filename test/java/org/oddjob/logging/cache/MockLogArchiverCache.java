package org.oddjob.logging.cache;

import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogListener;

public class MockLogArchiverCache implements LogArchiverCache {

	@Override
	public void addEvent(String archive, LogLevel level, String message) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void addLogListener(LogListener l, Object component, LogLevel level,
			long last, int history) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void destroy() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public long getLastMessageNumber(String archive) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public int getMaxHistory() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public boolean hasArchive(String archive) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	@Override
	public void removeLogListener(LogListener l, Object component) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
}
