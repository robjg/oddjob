package org.oddjob.state;


/**
 * Implementation of a {@link StateOperator} that is
 * either complete if all the children are complete, or not.
 * <p>
 * This is used by scheduling.
 * 
 * @author rob
 *
 */
public class CompleteOrNotOp implements StateOperator {
	
	public StateEvent evaluate(StateEvent... states) {

		new AssertNonDestroyed().evaluate(states);

		for (StateEvent state: states) {
			if (state.getState().isStoppable()) {
				return new StateEvent(state.getSource(), ParentState.ACTIVE);
			}
			if (state.getState().isReady()) {
				return new StateEvent(state.getSource(), ParentState.COMPLETE);
			}
			if (!state.getState().isComplete()) {
				return new StateEvent(state.getSource(), ParentState.INCOMPLETE);
			}
		}

		return new ConstStateful(ParentState.COMPLETE).lastStateEvent();
	}
}
