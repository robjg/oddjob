package org.oddjob.state;

/**
 * The {@link StateCondition}s that is always 
 * true.
 * <p>
 * Should this throw an IllegalStateException if
 * the condition is DESTROYED?
 * 
 * @author rob
 *
 */
public class IsAnyState implements StateCondition {

	public boolean test(JobState state) {
		return true;
	}
}
