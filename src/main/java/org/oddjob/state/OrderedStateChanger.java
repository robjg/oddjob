package org.oddjob.state;

import java.util.Date;

/**
 * A {@link StateChanger} that uses a {@link StateLock}
 * to ensure updates or ordered.
 * 
 * @author rob
 *
 */
public class OrderedStateChanger<S extends State> implements StateChanger<S> {

	private final StateChanger<S> stateChanger;
	private final StateLock stateLock;
	
	public OrderedStateChanger(StateChanger<S> stateChanger, StateLock stateLock) {
		this.stateChanger = stateChanger;
		this.stateLock = stateLock;
	}
	
	public void setStateException(final Throwable t, final Date date) {
		runLocked(() -> stateChanger.setStateException(t, date));
	}
	
	public void setStateException(final Throwable t) {
		runLocked(() -> stateChanger.setStateException(t));
	}
	
	public void setState(final S state, final Date date) {
		runLocked(() -> stateChanger.setState(state, date));
	}
	
	public void setState(final S state) {
		runLocked(() -> stateChanger.setState(state));
	}
	
	private void runLocked(Runnable runnable) {
		stateLock.waitToWhen(new IsAnyState(), runnable);
	}
}
