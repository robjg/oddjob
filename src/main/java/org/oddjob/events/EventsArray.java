package org.oddjob.events;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapper around an array of events that may or may not have happened.
 *
 * @param <T> The type of the event.
 */
public interface EventsArray<T> extends Iterable<Optional<EventOf<T>>> {

    int getSize();

    Optional<EventOf<T>> getEventAt(int index);

    Stream<Optional<EventOf<T>>> toStream();

    @Override
    default Iterator<Optional<EventOf<T>>> iterator() {
        return toStream().iterator();
    }
}
