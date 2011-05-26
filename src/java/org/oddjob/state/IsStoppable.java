package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a typical job
 * can be stopped.
 * 
 * @author rob
 *
 */
public class IsStoppable implements StateCondition {

	public boolean test(JobState state) {
		
		switch (state) {
		case EXECUTING:
			return true;
		default:
			return false;
		}
	}
	
}
