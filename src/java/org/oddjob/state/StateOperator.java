package org.oddjob.state;

import org.oddjob.Structural;

/**
 * An operation that provides the result of evaluating many states. These
 * are used by {@link Structural} jobs to decide their own state.
 * <p>
 * It is illegal to pass the {@link JobState#DESTROYED} state as
 * an argument. Behaviour is undefined in this instance.
 * 
 * @author rob
 *
 */
public interface StateOperator {

	/**
	 * Evaluate the given states.
	 * 
	 * @param states The states.
	 * @return The result state.
 	 */
	public ParentState evaluate(State... states);
	
}
