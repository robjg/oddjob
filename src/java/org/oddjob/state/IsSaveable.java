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
public class IsSaveable implements StateCondition {

	public boolean test(JobState state) {
		switch (state) {
		case READY:
		case COMPLETE:
		case INCOMPLETE:
		case EXCEPTION:
			return true;
		default:
			return false;
		}
	}
}
