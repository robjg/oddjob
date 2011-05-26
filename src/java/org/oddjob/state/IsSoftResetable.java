package org.oddjob.state;

/**
 * The {@link StateCondition}s under which a typical job
 * can be soft reset.
 * 
 * @author rob
 *
 */
public class IsSoftResetable implements StateCondition {

	public boolean test(JobState state) {
		
		switch (state) {
		case READY:
		case INCOMPLETE:
		case EXCEPTION:
			return true;
		default:
			return false;
		}
	}
	
}
