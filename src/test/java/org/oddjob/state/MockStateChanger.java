package org.oddjob.state;

import java.util.Date;


public class MockStateChanger implements StateChanger<ParentState> {

	public void setState(ParentState state) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setState(ParentState state, Date date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setStateException(Throwable t) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setStateException(Throwable t, Date date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
