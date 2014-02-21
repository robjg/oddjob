package org.oddjob.state;

import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.MockStateful;

public class StateExchangeTest extends TestCase {

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
		
		stateful.listener.jobStateChange(new StateEvent(stateful, ParentState.DESTROYED));
		
		assertEquals(ParentState.COMPLETE, changer.state);
		
		test.stop();
		
		assertNull(stateful.listener);
		
	}
}
