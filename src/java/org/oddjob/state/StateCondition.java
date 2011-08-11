package org.oddjob.state;

/**
 * Something that is able to test the condition of a {@link State}.
 * 
 * @author rob
 *
 */
public interface StateCondition {

	/**
	 * Tests the condition of the state.
	 * 
	 * @param state The state.
	 * @return true if the state matches the condition, false otherwise.
	 */
	public boolean test(State state);
}
