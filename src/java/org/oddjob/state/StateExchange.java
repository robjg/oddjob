package org.oddjob.state;

import java.util.Date;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;

/**
 * Pass on state. Generally used to reflect the state of children.
 * 
 * @author rob
 *
 */
public class StateExchange<T extends State> {

	private final StateChanger<T> recipient;
	
	private final Stateful source;
	
	private boolean running;

	private final StateListener stateListener = 
			new StateListener() {
		
		@Override
		public void jobStateChange(StateEvent event) {
			@SuppressWarnings("unchecked")
			T state = (T) event.getState();
			Date time = event.getTime();
		
			if (state.isDestroyed()) {
				// do nothing.
			}
			else if (state.isDestroyed()) {
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
		synchronized (this) {
			if (running) {
				return;
			}
			running = true;
		}
		source.addStateListener(stateListener);
	}
	
	public void stop() {
		synchronized (this) {
			running = false;
		}
		source.removeStateListener(stateListener);
	}
	
	public boolean isRunning() {
		synchronized(this) {
			return running;
		}
	}
}
