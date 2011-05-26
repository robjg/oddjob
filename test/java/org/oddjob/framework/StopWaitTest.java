package org.oddjob.framework;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.MockStateful;
import org.oddjob.state.JobStateHandler;

public class StopWaitTest extends TestCase {

	public void testStopWait() throws FailedToStopException {
		
		JobStateHandler stateful = new JobStateHandler(new MockStateful());
		
		new StopWait(stateful).run();
	}
}
