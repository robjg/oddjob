package org.oddjob;

import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class MockStateful implements Stateful {

	public void addJobStateListener(JobStateListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	public void removeJobStateListener(JobStateListener listener) {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
	
	@Override
	public JobStateEvent lastJobStateEvent() {
		throw new RuntimeException("Unexpected from class " + getClass());
	}
}
