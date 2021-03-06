package org.oddjob.state;

import org.junit.Test;

import java.util.Date;

import org.oddjob.OjTestCase;

import org.oddjob.MockStateful;

public class StateExchangeTest extends OjTestCase {

	private class OurStateful extends MockStateful {
		
		StateListener listener;
		
		public void addStateListener(StateListener listener) {
			assertNull(this.listener);
			this.listener = listener;
		}
		
		public void removeStateListener(StateListener listener) {
			assertEquals(this.listener, listener);
			this.listener = null;
		}
		
		
	}

	private class OurChanger extends MockStateChanger {

		ParentState state;
		
		@Override
		public void setState(ParentState state, Date date) {
			this.state = state;
		}
	}

	
	/**
	 * Destroyed not passed on. Should it be?
	 */
   @Test
	public void testDestroyedState() {
		
		OurStateful stateful = new OurStateful();

		OurChanger changer = new OurChanger();
		
		StateExchange<ParentState> test = 
				new StateExchange<ParentState>(stateful, changer);
		
		assertNull(stateful.listener);
		
		test.start();
		
		assertNotNull(stateful.listener);
		assertNull(changer.state);
		
		stateful.listener.jobStateChange(new StateEvent(stateful, ParentState.COMPLETE));
		
		assertEquals(ParentState.COMPLETE, changer.state);
		
		try {
			stateful.listener.jobStateChange(new StateEvent(stateful, ParentState.DESTROYED));
			fail("Should throw an Exception.");
		}
		catch (IllegalStateException e) {
			// expected.
		}
		assertEquals(ParentState.COMPLETE, changer.state);
		
		test.stop();
		
		assertNull(stateful.listener);
		
	}
}
