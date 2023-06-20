package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;

import java.util.Optional;

/**
 * A Stateful with just one State.
 */
public class ConstStateful implements Stateful {

    private final Object source;

    private final StateEvent stateEvent;

//    public ConstStateful(State state) {
//        this(null, state);
//    }
//
//    public ConstStateful(State state,  Throwable throwable) {
//        this(null, state, new Date(), throwable);
//    }
//
//    public ConstStateful(Object source, State state) {
//        this(source, state, StateInstant.now(), null);
//
//    }
//
//    @Deprecated(since="1.7", forRemoval=true)
//    public ConstStateful(Object source, State state, Date date) {
//        this(source, state, date.toInstant(), null);
//
//    }

//    @Deprecated(since="1.7", forRemoval=true)
//    public ConstStateful(Object source, State state, Date date, Throwable exception) {
//        this.source = source;
//        this.stateEvent = new StateEvent(this, state, date, exception);
//    }

    private ConstStateful(Object source, State state, StateInstant instant, Throwable exception) {
        this.source = source;
        this.stateEvent = StateEvent.exceptionAtInstant(this, state, instant, exception);
    }

    public static ConstStateful atInstant(Object source, State state, StateInstant instant) {
        return new ConstStateful(source, state, instant, null);
    }

    public static ConstStateful now(Object source, State state) {
        return new ConstStateful(source, state, StateInstant.now(), null);
    }

    public static ConstStateful exceptionAtInstant(Object source, State state, StateInstant instant, Throwable exception) {
        return new ConstStateful(source, state, instant, exception);
    }

    public static ConstStateful exceptionNow(Object source, State state, Throwable exception) {
        return new ConstStateful(source, state, StateInstant.now(), exception);
    }

    public static StateEvent event(State state) {
        return now(null, state).lastStateEvent();
    }

    public static StateEvent exception(State state, Throwable throwable) {
        return exceptionNow(null, state, throwable).lastStateEvent();
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
                        .map( s -> " for " + s)
                .orElse("");
    }
}
