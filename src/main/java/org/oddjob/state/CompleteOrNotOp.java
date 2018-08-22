package org.oddjob.state;


/**
 * Implementation of a {@link StateOperator} that is
 * either complete if all the children are complete, or not.
 * <p>
 * This is used by scheduling.
 * 
 * @author rob
 *
 */
public class CompleteOrNotOp implements StateOperator {
	
	public ParentState evaluate(State... states) {
		
		new AssertNonDestroyed().evaluate(states);
		
		for (State state: states) {
			if (state.isStoppable()) {
				return ParentState.ACTIVE;
			}
			if (state.isReady()) {
				return ParentState.COMPLETE;
			}
			if (!state.isComplete()) {
				return ParentState.INCOMPLETE;
			}
		}
		return ParentState.COMPLETE;
	}
}
