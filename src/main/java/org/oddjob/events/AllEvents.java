package org.oddjob.events;

import java.util.Optional;

/**
 * An operator that filters on all event. Used by event
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
            for (Optional<?> value : eventsArray) {
                if (!value.isPresent()) {
                    return false;
                }
            }
            return true;
        });
	}
}
