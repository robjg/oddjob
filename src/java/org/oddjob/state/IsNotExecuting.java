package org.oddjob.state;

/**
 * The {@link StateCondition}s when a job isn't executing.
 * 
 * @author rob
 *
 */
public class IsNotExecuting implements StateCondition {

	@Override
	public boolean test(State state) {
		
		return !state.isStoppable();
	}
	
}
