package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.function.Consumer;

/**
 * A source fo events.
 *
 * @param <T> The type of the event.
 */
public interface EventSource<T> {

    Restore subscribe(Consumer<? super T> consumer) throws Exception;
}
