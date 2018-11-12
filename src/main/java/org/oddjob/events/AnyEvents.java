package org.oddjob.events;

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

        super(list -> {
            for (T t : list) {
                if (t != null) {
                    return true;
                }
            }
            return false;
        });
    }
}
