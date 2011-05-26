package org.oddjob.state;

import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.MockStateful;

public class StateExchangeTest extends TestCase {

	private class OurStateful extends MockStateful {
		
		JobStateListener listener;
		
		public void addJobStateListener(JobStateListener listener) {
			assertNull(this.listener);
			this.listener = listener;
		}
		
		public void removeJobStateListener(JobStateListener listener) {
			assertEquals(this.listener, listener);
			this.listener = null;
		}
		
		
	}

	private class OurChanger extends MockStateChanger {

		JobState state;
		
		@Override
		public void setJobState(JobState state, Date date) {
			this.state = state;
		}
	}

	
	/**
	 * Destroyed not passed on. Should it be?
	 */
	public void testDestroyedState() {
		
		OurStateful stateful = new OurStateful();

		OurChanger changer = new OurChanger();
		
		StateExchange test = new StateExchange(stateful, changer);
		
		assertNull(stateful.listener);
		
		test.start();
		
		assertNotNull(stateful.listener);
		assertNull(changer.state);
		
		stateful.listener.jobStateChange(new JobStateEvent(stateful, JobState.COMPLETE));
		
		assertEquals(JobState.COMPLETE, changer.state);
		
		stateful.listener.jobStateChange(new JobStateEvent(stateful, JobState.DESTROYED));
		
		assertEquals(JobState.COMPLETE, changer.state);
		
		test.stop();
		
		assertNull(stateful.listener);
		
	}
}
