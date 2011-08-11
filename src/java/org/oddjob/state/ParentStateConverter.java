package org.oddjob.state;

/**
 * 
 * Provides a conversion from a {@link State} to an equivalent 
 * {@link ParentState}.
 * 
 * @author rob
 *
 */
public class ParentStateConverter {
	
	public ParentState toStructuralState(State state) {
		
		if (state.isReady()) {
			return ParentState.READY;
		}
		else if (state.isStoppable()) {
			return ParentState.ACTIVE;
		}
		else if (state.isIncomplete()) {
			return ParentState.INCOMPLETE;
		}
		else if (state.isComplete()) {
			return ParentState.COMPLETE;
		}
		else if (state.isException()) {
			return ParentState.EXCEPTION;
		}
		else if (state.isDestroyed()) {
			return ParentState.DESTROYED;
		}
		else {
			throw new IllegalStateException("Unconvertable state " + state);
		}
	}
}
