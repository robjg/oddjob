package org.oddjob.events;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapper around an array of events that may or may not have happened.
 *
 * @param <T> The type of the event.
 */
public interface EventsArray<T> extends Iterable<Optional<InstantEvent<T>>> {

    int getSize();

    Optional<InstantEvent<T>> getEventAt(int index);

    Stream<Optional<InstantEvent<T>>> toStream();

    @Override
    default Iterator<Optional<InstantEvent<T>>> iterator() {
        return toStream().iterator();
    }
}
