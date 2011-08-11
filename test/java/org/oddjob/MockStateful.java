package org.oddjob;

import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

public class MockStateful implements Stateful {

	public void addStateListener(StateListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public void removeStateListener(StateListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	@Override
	public StateEvent lastStateEvent() {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
}
