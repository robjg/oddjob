package org.oddjob.state;

import org.oddjob.Stateful;

/**
 * The Detail of an {@link StateEvent} without the source. Used as an abstraction for Serialisation
 * and for State Operators where the source will be changed.
 */
public interface StateDetail {

    State getState();

    StateInstant getStateInstant();

    Throwable getException();

    /**
     * Create a State Event from this detail and the given source. Not sure why we have this
     * and {@link StateEvent#fromDetail(Stateful, StateDetail)}.
     *
     * @param source The source of the new State Event.
     *
     * @return A new State Event.
     */
    default StateEvent toEvent(Stateful source) {
        return StateEvent.fromDetail(source, this);
    }
}
