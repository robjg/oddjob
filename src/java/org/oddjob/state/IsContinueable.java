package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a sequential type job
 * should continue to execute child jobs.
 * 
 * @author rob
 *
 */
public class IsContinueable implements StateCondition {
	
	public boolean test(JobState state) {
		
		switch (state) {
		case READY:
		case EXECUTING:
		case COMPLETE:
			return true;
		default:
			return false;
		}
	}
}
