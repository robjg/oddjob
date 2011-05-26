package org.oddjob.state;

import junit.framework.TestCase;

import org.oddjob.MockStateful;

public class SequentialHelperTest extends TestCase {

	private class OurStateful extends MockStateful {
		
		private final JobState jobState;
		
		JobStateListener listener;
		
		public OurStateful(JobState jobState) {
			this.jobState = jobState;
		}
		
		@Override
		public void addJobStateListener(JobStateListener listener) {
			assertNull(this.listener);
			assertNotNull(listener);
			this.listener = listener;
			listener.jobStateChange(new JobStateEvent(this, jobState));
		}
		
		@Override
		public void removeJobStateListener(JobStateListener listener) {
			assertNotNull(listener);
			assertEquals(this.listener, listener);
			this.listener = null;
		}
		
	}
	
	public void testAllStates() {
		
		SequentialHelper test = new SequentialHelper();
		
		OurStateful flag = new OurStateful(JobState.READY);
		
		assertTrue(test.canContinueAfter(flag));
		
		assertNull(flag.listener);
		
		assertTrue(test.canContinueAfter(new OurStateful(JobState.EXECUTING)));
		assertTrue(test.canContinueAfter(new OurStateful(JobState.COMPLETE)));
		assertFalse(test.canContinueAfter(new OurStateful(JobState.INCOMPLETE)));
		assertFalse(test.canContinueAfter(new OurStateful(JobState.EXCEPTION)));
		
		assertTrue(test.canContinueAfter(new Object()));
	}
	
}
