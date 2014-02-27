package org.oddjob.scheduling.state;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.ParentState;
import org.oddjob.state.ParentStateHandler;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

public class TimerStateAdapterTest extends TestCase {
	
	private class OurStateful implements Stateful {
		
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
	
	private class OurListener implements StateListener {
		
		State state;
		@Override
		public void jobStateChange(StateEvent event) {
			state = event.getState();
		}
	}
	
	public void testStateChanges() {
		
		final ParentStateHandler parentStateful = new ParentStateHandler(
				new OurStateful());
		
		TimerStateAdapter test = new TimerStateAdapter(parentStateful);		
		
		assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());
		
		OurListener listener = new OurListener();
		
		test.addStateListener(listener);
		
		parentStateful.callLocked(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				parentStateful.setState(ParentState.STARTED);
				parentStateful.fireEvent();
				return null;
			}
		});
		
		assertEquals(TimerState.STARTED, listener.state);
		
		parentStateful.callLocked(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				parentStateful.setState(ParentState.INCOMPLETE);
				parentStateful.fireEvent();
				return null;
			}
		});
		
		assertEquals(TimerState.INCOMPLETE, listener.state);
		
		parentStateful.callLocked(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				parentStateful.setState(ParentState.READY);
				parentStateful.fireEvent();
				return null;
			}
		});
		
		assertEquals(TimerState.STARTABLE, listener.state);
		
		parentStateful.callLocked(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				parentStateful.setState(ParentState.EXCEPTION);
				parentStateful.fireEvent();
				return null;
			}
		});
		
		assertEquals(TimerState.EXCEPTION, listener.state);
		
		parentStateful.callLocked(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				parentStateful.setState(ParentState.COMPLETE);
				parentStateful.fireEvent();
				return null;
			}
		});
		
		assertEquals(TimerState.COMPLETE, listener.state);
		
		test.removeStateListener(listener);
		
		parentStateful.callLocked(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				parentStateful.setState(ParentState.READY);
				parentStateful.fireEvent();
				return null;
			}
		});
		
		assertEquals(TimerState.COMPLETE, listener.state);
	}
}
