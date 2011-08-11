package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a sequential type job
 * should continue to execute child jobs.
 * 
 * @author rob
 *
 */
public class IsContinueable implements StateCondition {
	
	@Override
	public boolean test(State state) {
		
		return state.isPassable();
	}
}
