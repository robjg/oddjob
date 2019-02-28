package org.oddjob.events;

import java.util.Optional;

/**
 * @oddjob.decription An operator that filters on all event. Used by event
 * sources that combine multiple event
 * streams such as {@link ListSource} and {@link ForEvents}.
 *
 * @param <T> The type of event.
 *
 * @see EventOperator
 */
public class AllEvents<T> extends EventOperatorBase<T>{

	public AllEvents() {

	    super(eventsArray -> {
            for (Optional<EventOf<T>> value : eventsArray) {
                if (!value.isPresent()) {
                    return false;
                }
            }
            return true;
        });
	}
}
