package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a typical job
 * can be hard reset.
 * 
 * @author rob
 *
 */
public class IsHardResetable implements StateCondition {

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
