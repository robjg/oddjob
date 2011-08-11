package org.oddjob.state;

/**
 * Test if the job/service has finished.
 * 
 * @author rob
 *
 */
public class IsDone implements StateCondition {

	@Override
	public boolean test(State state) {
		if (state.isComplete() || 
				state.isIncomplete() ||
				state.isException()) {
			return true;
		}
		else {
			return false;
		}
	}
}
