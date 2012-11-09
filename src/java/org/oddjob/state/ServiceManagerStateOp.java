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
	
	@Override
	public ParentState evaluate(State... states) {
		
		new AssertNonDestroyed().evaluate(states);
		
		ParentState state = ParentState.READY;
		
		if (states.length > 0) {
			
			state = new ParentStateConverter().toStructuralState(states[0]);
			
			if (state.isDone()) {
				state = ParentState.COMPLETE;
			}
			
			for (int i = 1; i < states.length; ++i) {
				State next = states[i];

				if (state.isException() || next.isException()) {
					state = ParentState.EXCEPTION;
				}
				else if (state.isIncomplete() || next.isIncomplete()){
					state = ParentState.INCOMPLETE;
				}
				else if (state.isReady() || next.isReady()){
					state = ParentState.READY;
				}
				else if (!state.isDone() || !next.isDone()) {
					state = ParentState.ACTIVE;
				}
				else {
					state = ParentState.COMPLETE;
				}
			}
		}		
		return state;
	}
	
}
