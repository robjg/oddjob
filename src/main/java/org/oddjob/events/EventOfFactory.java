package org.oddjob.events;

/**
 * Something that can create an event of something.
 *
 * @param <T> The type the event is of.
 */
public interface EventOfFactory<T> {

    /**
     * Create an event of something.
     *
     * @param of The thing the event is of.
     *
     * @return The event of the thing.
     */
    EventOf<T> create(T of);
}
