package org.oddjob.events;

import java.util.List;
import java.util.stream.Stream;

/**
 * The result of combining {@link InstantEvent} events. This is also an {@link InstantEvent} the payload of which
 * may be a new thing or one of the things being combined.
 *
 * @param <T> The type of the events being combined.
 */
public interface CompositeEvent<T> extends InstantEvent<T> {

    /**
     * Get the event in the combination at the given index.
     *
     * @param index The index.
     *
     * @return The event.
     */
    InstantEvent<? extends T> getEvents(int index);

    /**
     * Get the number of events that have been combined.
     *
     * @return The number of events.
     */
    int getCount();

    /**
     * Get all the ofs from all the events in this composite and
     * it's children.
     *
     * @return A list of all the ofs.
     */
    List<T> getOfs();

    /**
     * Return the combined events as a stream.
     * @return
     */
    Stream<InstantEvent<? extends T>> stream();

    /**
     * Create a new instance from these events.
     *
     * @param eventOfs The events to create these from.
     *
     * @param <T> The type of the events.
     *
     * @return A new instance.
     */
    static <T> CompositeEvent<T> of(InstantEvent<T>... eventOfs) {
        return new CompositeEventList<>(eventOfs);
    }
}
