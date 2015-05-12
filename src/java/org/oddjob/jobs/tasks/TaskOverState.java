package org.oddjob.jobs.tasks;

import org.oddjob.state.State;
import org.oddjob.state.StateCondition;


public class TaskOverState implements StateCondition {

	@Override
	public boolean test(State state) {
		return state.isDestroyed() || 
				(state.isComplete() && !state.isStoppable());
	}
}
