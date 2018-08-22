package org.oddjob.state;

/**
 * Negates a {@link StateCondition}.
 * 
 * @author rob
 *
 */
public class IsNot implements StateCondition {

	private final StateCondition condition;
	
	public IsNot(StateCondition condition) {
		this.condition = condition;
	}	
	
	@Override
	public boolean test(State state) {
		return !condition.test(state);
	}
	
	@Override
	public String toString() {
		return "!" + condition;
	}
}
