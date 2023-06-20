package org.oddjob.state;

/**
 * 
 * Provides a conversion from a {@link State} to an equivalent 
 * {@link ParentState}.
 * <p>
 * Note that there is no parent state equivalent for 
 * {@link State#isExecuting()} because structural jobs should be 
 * {@link ParentState#ACTIVE} for this condition.
 * 
 * @author rob
 *
 */
public interface ParentStateConverter {
	
	/**
	 * Convert a state to an equivalent parent state.
	 * 
	 * @param state Any state.
	 * @return A Parent State.
	 */
	ParentState toStructuralState(State state);
}
