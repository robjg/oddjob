package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pass on state. Generally used to reflect the state of children.
 * 
 * @author rob
 *
 */
public class StateExchange<T extends State> {

	private final StateChanger<T> recipient;
	
	private final Stateful source;
	
	private final AtomicBoolean running = new AtomicBoolean();

	private final StateListener stateListener = 
			new StateListener() {
		
		@Override
		public void jobStateChange(StateEvent event) {
			@SuppressWarnings("unchecked")
			T state = (T) event.getState();
			StateInstant time = event.getStateInstant();

			if (state.isDestroyed()) {
				throw new IllegalStateException(
						"A StateOperator should never return a DESTROYED state.");
			}
			else if (state.isException()) {
				Throwable throwable = event.getException();
				recipient.setStateException(throwable, time);

			}
			else {
				recipient.setState(state, time);
			}
		}
	};
	
	public StateExchange(Stateful source, StateChanger<T> recipient) {
		this.source = source;
		this.recipient = recipient;
	}
	
	public void start() throws JobDestroyedException {
		if (running.compareAndSet(true, true)) {
			return;
		}
		source.addStateListener(stateListener);
	}
	
	public void stop() {
		running.set(false);
		source.removeStateListener(stateListener);
	}
	
	public boolean isRunning() {
			return running.get();
	}
}
