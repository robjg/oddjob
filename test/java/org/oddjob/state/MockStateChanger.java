package org.oddjob.state;

import java.util.Date;


public class MockStateChanger implements StateChanger {

	public void setJobState(JobState state) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setJobState(JobState state, Date date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setJobStateException(Throwable t) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void setJobStateException(Throwable t, Date date) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
