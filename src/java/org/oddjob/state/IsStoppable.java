package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a typical job
 * can be stopped.
 * 
 * @author rob
 *
 */
public class IsStoppable implements StateCondition {

	@Override
	public boolean test(State state) {
		
		return state.isStoppable();
	}
}
