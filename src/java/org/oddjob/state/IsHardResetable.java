package org.oddjob.state;

/**
 * The {@link StateCondition}s under witch a typical job
 * can be hard reset.
 * 
 * @author rob
 *
 */
public class IsHardResetable implements StateCondition {

	@Override
	public boolean test(State state) {
		
		return !state.isStoppable();
	}
	
}
