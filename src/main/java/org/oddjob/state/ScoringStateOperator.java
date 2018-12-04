package org.oddjob.state;

import org.oddjob.structural.OddjobChildException;

import java.util.Date;

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

        Throwable childException = childEvent.getException();
        OddjobChildException exception;

        if (childException == null) {
            exception = null;
        }
        else if (childException instanceof OddjobChildException) {
            exception = (OddjobChildException) childException;
        }
        else {
            exception = new OddjobChildException(childException,
                    childEvent.getSource().toString());
        }

        return new StateEvent(
                childEvent.getSource(),
                getParentStateConverter().toStructuralState(childEvent.getState()),
                new Date(),
                exception);
    }
}
