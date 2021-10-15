package org.oddjob.events;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapper around an array of events that may or may not have happened.
 *
 * @param <T> The type of the event.
 */
public interface EventsArray<T> extends Iterable<Optional<InstantEvent<? extends T>>> {

    int getSize();

    Optional<InstantEvent<? extends T>> getEventAt(int index);

    Stream<Optional<InstantEvent<? extends T>>> toStream();

    @Override
    default Iterator<Optional<InstantEvent<? extends T>>> iterator() {
        return toStream().iterator();
    }
}
