package org.oddjob.state;

/**
 * An operation that provides the result of evaluating many states.
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
	 * @return The result.
 	 */
	public JobState evaluate(JobState... states);
	
}
