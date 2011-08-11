package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a job
 * is executable.
 * 
 * @author rob
 *
 */
public class IsExecutable implements StateCondition {

	@Override
	public boolean test(State state) {

		return state.isReady();
	}
	
}
