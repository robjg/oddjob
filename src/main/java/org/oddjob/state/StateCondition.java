package org.oddjob.state;

import java.util.function.Predicate;

/**
 * Something that is able to test the condition of a {@link State}.
 * 
 * @author rob
 *
 */
@FunctionalInterface
public interface StateCondition extends Predicate<State> {

	/**
	 * Tests the condition of the state.
	 * 
	 * @param state The state.
	 * @return true if the state matches the condition, false otherwise.
	 */
	@Override
	boolean test(State state);

	/**
	 * Helper to save type getState all the time.
	 *
	 * @param stateEvent The event.
	 * @return depends on the event state
	 */
	default boolean test(StateEvent stateEvent) {
		return test(stateEvent.getState());
	}
}
