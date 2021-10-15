package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.List;
import java.util.function.Consumer;

/**
 * Subscribe to a list of event sources and applies some logic to their event
 * to possibly propagate a new event.
 *
 * @param <T> The type of the event.
 */
public interface EventOperator<T> {

    Restore start(List<? extends InstantEventSource<? extends T>> nodes,
                  Consumer<? super CompositeEvent<T>> results)
            throws Exception;

}
