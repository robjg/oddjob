package org.oddjob.state;

import org.oddjob.Structural;

/**
 * Implementation of a {@link StateOperator} that provides the worst
 * of any given states. This Operator is used in many {@link Structural}
 * jobs to calculate parent state.
 * 
 * @author rob
 *
 */
public class WorstStateOp implements StateOperator {
	
	@Override
	public ParentState evaluate(State... states) {
		
		new AssertNonDestroyed().evaluate(states);
		
		ParentState state = ParentState.READY;
		
		if (states.length > 0) {
			
			state = new ParentStateConverter().toStructuralState(states[0]);
			
			for (int i = 1; i < states.length; ++i) {
				State next = states[i];

				if (state.isStoppable() || next.isStoppable()){
					state = ParentState.ACTIVE;
				}
				else if (state.isException() || next.isException()) {
					state = ParentState.EXCEPTION;
				}
				else if (state.isIncomplete() || next.isIncomplete()){
					state = ParentState.INCOMPLETE;
				}
				else if (state.isReady() || next.isReady()){
					state = ParentState.READY;
				}
				else {
					state = ParentState.COMPLETE;
				}
			}
		}
		
		return state;
	}
	
}
