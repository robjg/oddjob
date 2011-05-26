package org.oddjob.monitor.model;

import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;

public class MockExplorerContext implements ExplorerContext {

	public ExplorerContext addChild(Object child) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public Object getThisComponent() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ThreadManager getThreadManager() {
		throw new RuntimeException("Unexpected from " + getClass());
	}

	public ExplorerContext getParent() {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public Object getValue(String key) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setValue(String key, Object value) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
}
