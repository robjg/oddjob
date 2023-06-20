package org.oddjob.scheduling.state;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateOperator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Stateful} that adapts a {@link ParentState}s to 
 * {@link TimerState}s. Required because {@link StateOperator}s provide
 * {@code ParentState}s but timers need to reflect the child states as a
 * {@code TimerState}.
 * 
 * @author rob
 *
 */
public class TimerStateAdapter implements Stateful {

	private final Stateful adapting;
	
	private final Map<StateListener, StateListener> adaptedListeners
		= new ConcurrentHashMap<>();
	
	/**
	 * Create a new instance.
	 * 
	 * @param adapting The {@link Stateful} that provides 
	 * {@link ParentState}s.
	 */
	public TimerStateAdapter(Stateful adapting) {
		this.adapting = adapting;
	}
	
	/**
	 * Convert the event that is assumed to contain a {@link ParentState}
	 * into an equivalent event that contains a {@link TimerState}.
	 * 
	 * @param event The original event.
	 * @return The new event.
	 */
	protected StateEvent convert(StateEvent event) {

		ParentState parentState = (ParentState) event.getState();
		TimerState timerState;
		
		switch (parentState) {
		case ACTIVE:
			timerState = TimerState.ACTIVE;
			break;
		case COMPLETE:
			timerState = TimerState.COMPLETE;
			break;
		case EXCEPTION:
			timerState = TimerState.EXCEPTION;
			break;
		case INCOMPLETE:
			timerState = TimerState.INCOMPLETE;
			break;
		case READY:
			timerState = TimerState.STARTABLE;
			break;
		case STARTED:
			timerState = TimerState.STARTED;
			break;
		case EXECUTING:
		case DESTROYED:
		default:
			throw new IllegalStateException(parentState.toString());
		}
		return event.copy()
				.withState(timerState)
				.create();
	}
	
	@Override
	public StateEvent lastStateEvent() {
		return convert(adapting.lastStateEvent());
	}
	
	@Override
	public void addStateListener(final StateListener listener)
			throws JobDestroyedException {
		StateListener adaptedListener =
				event -> listener.jobStateChange(convert(event));
		adaptedListeners.put(listener, adaptedListener);
		adapting.addStateListener(adaptedListener);
	}
	
	@Override
	public void removeStateListener(StateListener listener) {
		StateListener adaptedListener = adaptedListeners.remove(listener);
		if (adaptedListener == null) {
			return;
		}
		
		adapting.removeStateListener(adaptedListener);
	}
}
