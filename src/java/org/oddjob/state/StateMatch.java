package org.oddjob.state;

/**
 * Provide a very simple test on a State.
 * 
 * @author rob
 *
 */
public class StateMatch implements StateCondition {

	final private State match;
	
	public StateMatch(State match) {
		this.match = match;
	}
	
	@Override
	public boolean test(State state) {
		if (state == match) {
			return true;
		}
		
		if (state.isReady() != match.isReady()) {
			return false;
		}
		
		if (state.isExecuting() != match.isExecuting()) {
			return false;
		}
		
		if (state.isStoppable() != match.isStoppable()) {
			return false;
		}
		
		if (state.isComplete() != match.isComplete()) {
			return false;
		}
		
		if (state.isIncomplete() != match.isIncomplete()) {
			return false;
		}
		
		if (state.isException() != match.isException()) {
			return false;
		}
		
		if (state.isDestroyed() != match.isDestroyed()) {
			return false;
		}
		
		return true;
	}
}
