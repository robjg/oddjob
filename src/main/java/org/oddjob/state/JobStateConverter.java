package org.oddjob.state;

/**
 * Provides a conversion from a {@link State} to an equivalent 
 * {@link JobState}.
 * 
 * @author rob
 *
 */
public class JobStateConverter {
	
	public JobState toJobState(State state) {
		
		if (state.isReady()) {
			return JobState.READY;
		}
		else if (state.isStoppable()) {
			return JobState.EXECUTING;
		}
		else if (state.isIncomplete()) {
			return JobState.INCOMPLETE;
		}
		else if (state.isComplete()) {
			return JobState.COMPLETE;
		}
		else if (state.isException()) {
			return JobState.EXCEPTION;
		}
		else if (state.isDestroyed()) {
			return JobState.DESTROYED;
		}
		else {
			throw new IllegalStateException("Unconvertable state " + state);
		}
	}
}
