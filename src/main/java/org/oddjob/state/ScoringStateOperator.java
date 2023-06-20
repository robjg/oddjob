package org.oddjob.state;

import org.oddjob.structural.OddjobChildException;

/**
 * A base state operator for state evaluation using scores for
 * different states.
 */
abstract public class ScoringStateOperator implements StateOperator {

    abstract protected int score(ParentState state);

    abstract protected ParentStateConverter getParentStateConverter();

    protected int scoreAndCheck(ParentState state) {
        int score = score(state);
        if (score < 0) {
            throw new IllegalStateException("Unexpected State " + state);
        }
        return score;
    }

    @Override
    public StateEvent evaluate(StateEvent... states) {

        if (states.length == 0) {
            return null;
        }

        StateEvent stateEvent = processEvent(states[0]);

        int maxSoFar = scoreAndCheck((ParentState) stateEvent.getState());

        for (int i = 1; i < states.length; ++i) {

            ParentState next = getParentStateConverter().toStructuralState(
                    states[i].getState());

            int nextScore = scoreAndCheck(next);
            if (nextScore > maxSoFar) {
                maxSoFar = nextScore;
                stateEvent = processEvent(states[i]);
            }
        }

        return stateEvent;
    }

    StateEvent processEvent(StateEvent childEvent) {

        ParentState parentState = getParentStateConverter().toStructuralState(childEvent.getState());

        Throwable childException = childEvent.getException();
        OddjobChildException exception;

        if (childException == null) {
            return childEvent.copy()
                    .withState(parentState)
                    .create();
        }
        else if (childException instanceof OddjobChildException) {
            return childEvent.copy()
                    .withExceptionState(parentState, childException)
                    .create();
        }
        else {
            return childEvent.copy()
                    .withExceptionState(parentState,
                    new OddjobChildException(childException, childEvent.getSource().toString()))
                    .create();
        }
    }
}
