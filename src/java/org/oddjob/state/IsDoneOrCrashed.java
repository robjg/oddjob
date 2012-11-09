package org.oddjob.state;

/**
 * Test if the job/service has finished or has crashed.
 * 
 * @author rob
 *
 */
public class IsDoneOrCrashed implements StateCondition {

	@Override
	public boolean test(State state) {
		if (state.isDone() || 
				state.isIncomplete() ||
				state.isException()) {
			return true;
		}
		else {
			return false;
		}
	}
}
