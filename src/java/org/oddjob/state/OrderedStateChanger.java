package org.oddjob.state;

import java.util.Date;

/**
 * A {@link StateChanger} that uses a {@link StateLock}
 * to ensure updates or ordered.
 * 
 * @author rob
 *
 */
public class OrderedStateChanger implements StateChanger {

	private final StateChanger stateChanger;
	private final StateLock stateLock;
	
	public OrderedStateChanger(StateChanger stateChanger, StateLock stateLock) {
		this.stateChanger = stateChanger;
		this.stateLock = stateLock;
	}
	
	public void setJobStateException(final Throwable t, final Date date) {
		runLocked(new Runnable() {
				public void run() {
					stateChanger.setJobStateException(t, date);
				}
			});
	}
	
	public void setJobStateException(final Throwable t) {
		runLocked(new Runnable() {
			public void run() {
				stateChanger.setJobStateException(t);
			}
		});
	}
	
	public void setJobState(final JobState state, final Date date) {
		runLocked(new Runnable() {
			public void run() {
				stateChanger.setJobState(state, date);
			}
		});
	}
	
	public void setJobState(final JobState state) {
		runLocked(new Runnable() {
			public void run() {
				stateChanger.setJobState(state);
			}
		});
	}
	
	private void runLocked(Runnable runnable) {
		try {
			stateLock.waitToWhen(new IsAnyState(), runnable);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
