package org.oddjob.state;

/**
 * Implementation of a {@link StateOperator} that provides logical 'and'
 * like functionality.
 * 
 * @author rob
 *
 */
public class AndStateOp implements StateOperator {
	
	private final ParentStateConverter parentStateConverter;
	
	public AndStateOp() {
		this(new StandardParentStateConverter());
	}
	
	public AndStateOp(ParentStateConverter parentStateConverter) {
		this.parentStateConverter = parentStateConverter;
	}
	
	@Override
	public ParentState evaluate(State... states) {
		
		new AssertNonDestroyed().evaluate(states);
		
		ParentState state = ParentState.READY;
		
		if (states.length > 0) {
			
			state = parentStateConverter.toStructuralState(states[0]);
			
			for (int i = 1; i < states.length; ++i) {
				State next = states[i];
				
				if (state.isStoppable() || next.isStoppable()){
					state = ParentState.ACTIVE;
				}
				else if (state.isException() || next.isException()) {
					state = ParentState.EXCEPTION;
				}
				else if (state.isIncomplete() && next.isIncomplete()){
					state = ParentState.INCOMPLETE;
				}
				else if (state.isComplete() && next.isComplete()){
					state = ParentState.COMPLETE;
				}
				else {
					state = ParentState.READY;
				}
			}
		}
		
		return state;
	}
	
}
