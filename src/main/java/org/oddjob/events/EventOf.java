package org.oddjob.events;

import java.awt.*;
import java.time.Instant;

/**
 * An event from the event framework. Implementations are generally wrappers
 * around other events or wrappers around objects that are triggers.
 *
 * @param <T> The type of the event or trigger.
 */
public interface EventOf<T> {

    /**
     * Get the underlying event or trigger. Will not be null.
     *
     * @return The event/trigger.
     */
    T getOf();

    /**
     * Get the UTC time of the event.
     *
     * @return The time.
     */
    Instant getTime();


    static <T> EventOf<T> of(T item, Instant time) {
        return new WrapperOf<>(item, time);
    }

    static <T> EventOf<T> of(T item) {
        return new WrapperOf<>(item, Instant.now());
    }

}
