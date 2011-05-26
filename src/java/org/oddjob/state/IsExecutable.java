package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a job
 * is executable.
 * 
 * @author rob
 *
 */
public class IsExecutable implements StateCondition {

	public boolean test(JobState state) {
		
		switch (state) {
		case READY:
			return true;
		default:
			return false;
		}
	}
	
}
