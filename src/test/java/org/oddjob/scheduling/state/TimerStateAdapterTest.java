package org.oddjob.scheduling.state;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.*;

public class TimerStateAdapterTest extends OjTestCase {
	
	private static class OurStateful implements Stateful {
		
		@Override
		public StateEvent lastStateEvent() {
			throw new RuntimeException("Unexpected!");
		}
		
		@Override
		public void addStateListener(StateListener listener)
				throws JobDestroyedException {
			throw new RuntimeException("Unexpected!");
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			throw new RuntimeException("Unexpected!");
		}
	}
	
	private static class OurListener implements StateListener {
		
		State state;
		@Override
		public void jobStateChange(StateEvent event) {
			state = event.getState();
		}
	}
	
   @Test
	public void testStateChanges() {
		
		final ParentStateHandler parentStateful = new ParentStateHandler(
				new OurStateful());
		
		TimerStateAdapter test = new TimerStateAdapter(parentStateful);		
		
		assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());
		
		OurListener listener = new OurListener();
		
		test.addStateListener(listener);
		
		parentStateful.runLocked(() -> {
			parentStateful.setState(ParentState.STARTED);
			parentStateful.fireEvent();
		});
		
		assertEquals(TimerState.STARTED, listener.state);
		
		parentStateful.runLocked(() -> {
			parentStateful.setState(ParentState.INCOMPLETE);
			parentStateful.fireEvent();
		});
		
		assertEquals(TimerState.INCOMPLETE, listener.state);
		
		parentStateful.runLocked(() -> {
			parentStateful.setState(ParentState.READY);
			parentStateful.fireEvent();
		});
		
		assertEquals(TimerState.STARTABLE, listener.state);
		
		parentStateful.runLocked(() -> {
			parentStateful.setStateException(ParentState.EXCEPTION, new RuntimeException());
			parentStateful.fireEvent();
		});
		
		assertEquals(TimerState.EXCEPTION, listener.state);
		
		parentStateful.runLocked(() -> {
			parentStateful.setState(ParentState.COMPLETE);
			parentStateful.fireEvent();
		});
		
		assertEquals(TimerState.COMPLETE, listener.state);
		
		test.removeStateListener(listener);
		
		parentStateful.runLocked(() -> {
			parentStateful.setState(ParentState.READY);
			parentStateful.fireEvent();
		});
		
		assertEquals(TimerState.COMPLETE, listener.state);
	}
}
