package org.oddjob.state;

import junit.framework.TestCase;

import org.oddjob.MockStateful;

public class SequentialHelperTest extends TestCase {

	private class OurStateful extends MockStateful {
		
		private final JobState jobState;
		
		public OurStateful(JobState jobState) {
			this.jobState = jobState;
		}
		
		@Override
		public StateEvent lastStateEvent() {
			return new StateEvent(this, jobState);
		}		
	}
	
	public void testAllStates() {
		
		SequentialHelper test = new SequentialHelper();
		
		OurStateful flag = new OurStateful(JobState.READY);
		
		assertTrue(test.canContinueAfter(flag));
		
		assertTrue(test.canContinueAfter(new OurStateful(JobState.EXECUTING)));
		assertTrue(test.canContinueAfter(new OurStateful(JobState.COMPLETE)));
		assertFalse(test.canContinueAfter(new OurStateful(JobState.INCOMPLETE)));
		assertFalse(test.canContinueAfter(new OurStateful(JobState.EXCEPTION)));
		
		assertTrue(test.canContinueAfter(new Object()));
	}
	
}
