package org.oddjob.scheduling.state;

import org.oddjob.state.State;

/**
 * 
 * Provides a conversion from a {@link State} to an equivalent 
 * {@link TimerState}.
 * <p>
 * Note that there is no parent state equivalent for 
 * {@link State#isExecuting()} because structural jobs should be 
 * {@link TimerState#ACTIVE} for this condition.
 * 
 * @author rob
 *
 */
public interface TimerStateConverter {
	
	/**
	 * Convert a state to an equivalent parent state.
	 * 
	 * @param state Any state.
	 * @return A Parent State.
	 */
	public TimerState toStructuralState(State state);
}
