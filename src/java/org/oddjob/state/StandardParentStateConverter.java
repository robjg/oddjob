package org.oddjob.state;

/**
 * The typical {@link ParentStateConverter}.
 * 
 * @author rob
 *
 */
public class StandardParentStateConverter implements ParentStateConverter {
	
	@Override
	public ParentState toStructuralState(State state) {
		
		if (state.isDestroyed()) {
			return ParentState.DESTROYED;
		}
		else if (state.isStoppable()) {
			if (state.isComplete()) {
				return ParentState.STARTED;
			}
			else {
				return ParentState.ACTIVE;
			}
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
		else if (state.isReady()) {
			return ParentState.READY;
		}
		else {
			throw new IllegalStateException("Unconvertable state " + state);
		}
	}
}
