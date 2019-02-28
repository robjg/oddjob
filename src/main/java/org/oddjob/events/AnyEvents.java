package org.oddjob.events;

import java.util.Optional;

/**
 * @oddjob.decription An operator that filters on any event. Used by event sources that combine multiple event
 * streams such as {@link ListSource} and {@link ForEvents}.
 *
 * @param <T> The type of event.
 *
 * @see EventOperator
 */
public class AnyEvents<T> extends EventOperatorBase<T> {

    public AnyEvents() {

        super(eventsArray -> {
            for (Optional<EventOf<T>> t : eventsArray) {
                if (t.isPresent()) {
                    return true;
                }
            }
            return false;
        });
    }
}
