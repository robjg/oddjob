package org.oddjob.state;

/**
 * The {@link StateCondition}s when a job isn't executing.
 * 
 * @author rob
 *
 */
public class IsNotExecuting implements StateCondition {

	public boolean test(JobState state) {
		
		switch (state) {
		case EXECUTING:
			return false;
		default:
			return true;
		}
	}
	
}
