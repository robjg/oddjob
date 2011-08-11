package org.oddjob.state;

/**
 * Implementation of a {@link StateOperator} that provides logical 'and'
 * like functionality.
 * 
 * @author rob
 *
 */
public class OrStateOp implements StateOperator {
	
	@Override
	public ParentState evaluate(State... states) {
		new AssertNonDestroyed().evaluate(states);
		
		ParentState state = ParentState.READY;
		
		for (int i = 0; i < states.length; ++i) {
			State next = states[i];
			
			if (state.isStoppable() || next.isStoppable()){
				state = ParentState.ACTIVE;
			}
			else if (state.isException() || next.isException()) {
				state = ParentState.EXCEPTION;
			}
			else if (state.isComplete() || next.isComplete()){
				state = ParentState.COMPLETE;
			}
			else if (state.isIncomplete() || next.isIncomplete()){
				state = ParentState.INCOMPLETE;
			}
		}
		
		return state;
	}
	
}
