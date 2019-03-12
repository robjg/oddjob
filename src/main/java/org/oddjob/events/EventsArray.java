package org.oddjob.events;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapper around an array of events that may or may not have happened.
 *
 * @param <T> The type of the event.
 */
public interface EventsArray<T> extends Iterable<Optional<EventOf<? extends T>>> {

    int getSize();

    Optional<EventOf<? extends T>> getEventAt(int index);

    Stream<Optional<EventOf<? extends T>>> toStream();

    @Override
    default Iterator<Optional<EventOf<? extends T>>> iterator() {
        return toStream().iterator();
    }
}
