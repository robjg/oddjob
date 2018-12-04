package org.oddjob.state;

import org.oddjob.Structural;

/**
 * Implementation of a {@link StateOperator} that provides a parent state
 * as follows:
 * <ul>
 * <li>If any child is EXCEPTION then evaluate to EXCEPTION.</li>
 * <li>If any child is INCOMPLETE then evaluate to INCOMPLETE.</li>
 * <li>If any child is READY then evaluate to READY.</li>
 * <li>If any child is ACTIVE/EXECUTING then evaluate to ACTIVE.</li>
 * <li>Otherwise all children must be COMPLETE or STARTED so evaluate 
 * to COMPLETE.</li>
 * </ul>
 * 
 * <p>
 * This Operator is used in many {@link Structural}
 * jobs to calculate parent state.
 * 
 * @author rob
 *
 */
public class ServiceManagerStateOp implements StateOperator {

	private static class ServiceManagerParentStateConverter
	implements ParentStateConverter {
		
		@Override
		public ParentState toStructuralState(State state) {
			if (state.isDestroyed()) {
				return ParentState.DESTROYED;
			}
			else if (state.isIncomplete()) {
				return ParentState.INCOMPLETE;
			}
			else if (state.isException()) {
				return ParentState.EXCEPTION;
			}
			else if (state.isComplete()) {
					return ParentState.COMPLETE;
			}
			else if (state.isExecuting()) {
				return ParentState.ACTIVE;
			}
			else if (state.isStoppable()) { 
				return ParentState.COMPLETE;
			}
			else if (state.isReady()) {
				return ParentState.READY;
			}
			else {
				throw new IllegalStateException("Unconvertable state " + 
						state);
			}
		}
	}

	@Override
	public StateEvent evaluate(StateEvent... states) {
		return new WorstStateOp(new ServiceManagerParentStateConverter()
		).evaluate(states);
	}

	public String toString() {
		
		return getClass().getSimpleName();
	}
}
