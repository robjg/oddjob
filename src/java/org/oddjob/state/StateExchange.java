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
public class StateExchange {

	private final StateChanger<ParentState> recipient;
	
	private final Stateful source;
	
	private boolean running;

	private final StateListener stateListener = 
			new StateListener() {
		
		@Override
		public void jobStateChange(StateEvent event) {
			ParentState state = (ParentState) event.getState();
			Date time = event.getTime();
		
			switch (state) {
				case DESTROYED:
					break;
				case EXCEPTION:
					Throwable throwable = event.getException();
					recipient.setStateException(throwable, time);
					break;
				default:
					recipient.setState(state, time);
					break;
			}
		}
	};
	
	public StateExchange(Stateful source, StateChanger<ParentState> recipient) {
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
