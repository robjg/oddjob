package org.oddjob.events;

import org.oddjob.arooa.types.ValueFactory;

import java.time.Clock;
import java.util.Optional;

/**
 * Provide a {@link EventOfFactory}.
 *
 * @param <T> The type the events will be of.
 */
public final class WrapperOfFactory<T>
        implements ValueFactory<EventOfFactory<T>>{

    private volatile Clock clock;

    @Override
    public EventOfFactory<T> toValue() {

        Clock clock = Optional.ofNullable(this.clock)
                              .orElse(Clock.systemUTC());

        return event -> new WrapperOf<>(event, clock.instant());
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

}