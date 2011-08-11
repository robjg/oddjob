package org.oddjob.state;

import junit.framework.TestCase;

import org.oddjob.MockStateful;

public class SequentialHelperTest extends TestCase {

	private class OurStateful extends MockStateful {
		
		private final JobState jobState;
		
		StateListener listener;
		
		public OurStateful(JobState jobState) {
			this.jobState = jobState;
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			assertNull(this.listener);
			assertNotNull(listener);
			this.listener = listener;
			listener.jobStateChange(new StateEvent(this, jobState));
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
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
