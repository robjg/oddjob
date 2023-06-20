package org.oddjob.state.expr;

import org.oddjob.events.InstantEvent;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * The result of evaluating a state event against some condition.
 *
 * @see StateExpression
 */
public class StateEvaluation implements InstantEvent<Boolean> {

    private final Boolean result;

    private final StateEvent stateEvent;

    public StateEvaluation(Boolean result, StateEvent stateEvent) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(stateEvent);

        this.result = result;
        this.stateEvent = stateEvent;
    }

    @Override
    public Boolean getOf() {
        return result;
    }

    public State getState() {
        return stateEvent.getState();
    }

    public Throwable getException() {
        return stateEvent.getException();
    }

    @Override
    public Instant getTime() {
        return stateEvent.getInstant();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateEvaluation that = (StateEvaluation) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(stateEvent, that.stateEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, stateEvent);
    }

    @Override
    public String toString() {
        return "StateEvaluation{" +
                "result=" + result +
                ", stateEvent=" + stateEvent +
                '}';
    }
}
