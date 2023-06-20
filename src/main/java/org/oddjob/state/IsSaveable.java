package org.oddjob.state;

/**
 * The {@link StateCondition}s that is always 
 * true.
 * <p>
 * Should this throw an IllegalStateException if
 * the condition is DESTROYED?
 * 
 * @see BaseStateChanger
 * 
 * @author rob
 *
 */
public class IsSaveable implements StateCondition {

	public static boolean state(State state) {

		return state.isReady() ||
				state.isComplete() ||
				state.isIncomplete() ||
				state.isException();
	}

	@Override
	public boolean test(State state) {

		return state(state);
	}
}

