package org.oddjob.state;

/**
 * The {@link StateCondition}s that is always 
 * true.
 * <p>
 * Should this throw an IllegalStateException if
 * the condition is DESTROYED?
 * 
 * @author rob
 *
 */
public class IsSaveable implements StateCondition {

	@Override
	public boolean test(State state) {
		
		return state.isReady() ||
		state.isComplete() ||
		state.isIncomplete() ||
		state.isException();
	}
}

