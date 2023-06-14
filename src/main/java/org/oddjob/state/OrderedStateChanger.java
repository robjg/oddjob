package org.oddjob.state;

import org.oddjob.framework.JobDestroyedException;

import java.time.Instant;

/**
 * A {@link StateChanger} that uses a {@link StateLock}
 * to ensure updates or ordered.
 *
 * @author rob
 */
public class OrderedStateChanger<S extends State> implements StateChanger<S> {

    private final StateChanger<S> stateChanger;
    private final StateLock stateLock;

    public OrderedStateChanger(StateChanger<S> stateChanger, StateLock stateLock) {
        this.stateChanger = stateChanger;
        this.stateLock = stateLock;
    }

    @Override
    public void setState(final S state) {
        stateLock.runLocked(
                () -> stateChanger.setState(state));
    }

    @Override
    public void setState(S state, Instant instant) throws JobDestroyedException {
        stateLock.runLocked(() -> stateChanger.setState(state, instant));
    }

    @Override
    public void setStateException(final Throwable t) {
        stateLock.runLocked(() -> stateChanger.setStateException(t));
    }

    @Override
    public void setStateException(Throwable t, Instant instant) throws JobDestroyedException {
        stateLock.runLocked(
                () -> stateChanger.setStateException(t, instant));
    }

}
