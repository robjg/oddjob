package org.oddjob.state;

import java.time.Instant;


public class MockStateChanger implements StateChanger<ParentState> {

	public void setState(ParentState state) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setState(ParentState state, Instant date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setStateException(Throwable t) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setStateException(Throwable t, Instant date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
