package org.oddjob.state;


/**
 * The {@link StateCondition}s under which a typical job
 * can be soft reset.
 * 
 * @author rob
 *
 */
public class IsSoftResetable implements StateCondition {

	@Override
	public boolean test(State state) {
		
		return !state.isStoppable() && !state.isComplete();
	}	
}
