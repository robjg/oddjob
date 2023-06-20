package org.oddjob.state;

public class MockStateChanger implements StateChanger<ParentState> {

	public void setState(ParentState state) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setState(ParentState state, StateInstant date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setStateException(Throwable t) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setStateException(Throwable t, StateInstant date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
