package org.oddjob.state;

/**
 * Implementation of a {@link StateOperator} that provides logical 'and'
 * like functionality.
 *
 * @author rob
 */
public class AndStateOp implements StateOperator {

    private final ParentStateConverter parentStateConverter;

    public AndStateOp() {
        this(new StandardParentStateConverter());
    }

    public AndStateOp(ParentStateConverter parentStateConverter) {
        this.parentStateConverter = parentStateConverter;
    }

    @Override
    public StateEvent evaluate(StateEvent... states) {

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
                continue;
            }

            if (state.getState().isException()) {
                continue;
            }

            if (next.getState().isException()) {
                state = next;
                continue;
            }

            if (state.getState().isIncomplete() && next.getState().isIncomplete()) {
                continue;
            }

            if (state.getState().isComplete() && next.getState().isComplete()) {
                continue;
            }

            state = new StateEvent(state.getSource(), ParentState.READY);
        }

        return StateOperator.toParentEvent(state, parentStateConverter);
    }

}
