package org.oddjob.state;

/**
 * Implementation of a {@link StateOperator} that provides logical 'and'
 * like functionality.
 * 
 * @author rob
 *
 */
public class OrStateOp implements StateOperator {

	private final ParentStateConverter parentStateConverter;

	public OrStateOp(ParentStateConverter parentStateConverter) {
		this.parentStateConverter = parentStateConverter;
	}

	public OrStateOp() {
		this(new StandardParentStateConverter());
	}

	@Override
	public StateEvent evaluate(StateEvent... states) {

		new AssertNonDestroyed().evaluate(states);

		if (states.length == 0) {
			return null;

		}

		StateEvent state = states[0];
		
		for (int i = 1; i < states.length; ++i) {
			StateEvent next = states[i];
			
			if (state.getState().isStoppable()) {
				continue;
			}

			if (next.getState().isStoppable()) {
				state = next;
			}
			else if (state.getState().isException()) {
				continue;
			}

			if (next.getState().isException()) {
				state = next;
			}
			else if (state.getState().isComplete()) {
				continue;
			}

			if (next.getState().isComplete()){
				state = next;
			}
			else if (state.getState().isIncomplete()) {
				continue;
			}

			if (next.getState().isIncomplete()) {
				state = next;
			}
		}
		
		return StateOperator.toParentEvent(state, parentStateConverter);
	}
}
