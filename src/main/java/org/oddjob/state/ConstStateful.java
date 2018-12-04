package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;

import java.util.Date;
import java.util.Optional;

/**
 * A Stateful with just one State.
 */
public class ConstStateful implements Stateful {

    private final Object source;

    private final StateEvent stateEvent;

    public ConstStateful(State state) {
        this(null, state);
    }

    public ConstStateful(State state,  Throwable throwable) {
        this(null, state, new Date(), throwable);
    }

    public ConstStateful(Object source, State state) {
        this(source, state, new Date());

    }
    public ConstStateful(Object source, State state, Date date) {
        this(source, state, date, null);

    }

    public ConstStateful(Object source, State state, Date date, Throwable exception) {
        this.source = source;
        this.stateEvent = new StateEvent(this, state, date, exception);
    }

    public static StateEvent event(State state) {
        return new ConstStateful(state).lastStateEvent();
    }

    public static StateEvent exception(State state, Throwable throwable) {
        return new ConstStateful(state,
                throwable).lastStateEvent();
    }

    @Override
    public void addStateListener(StateListener listener) throws JobDestroyedException {
        listener.jobStateChange(stateEvent);
    }

    @Override
    public void removeStateListener(StateListener listener) {

    }

    @Override
    public StateEvent lastStateEvent() {
        return stateEvent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                Optional.ofNullable( source )
                        .map( s -> " for " + s.toString())
                .orElse("");
    }
}
