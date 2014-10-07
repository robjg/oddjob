package org.oddjob.state;


/**
 * The {@link StateCondition}s under which a typical job
 * can be forced to the complete state.
 * <p>
 * Note that this is currently under the same conditions as 
 * {@link IsSoftResetable}.
 * 
 * @author rob
 *
 */
public class IsForceable implements StateCondition {

	@Override
	public boolean test(State state) {
		
		return !state.isStoppable() && !state.isComplete();
	}	
}
