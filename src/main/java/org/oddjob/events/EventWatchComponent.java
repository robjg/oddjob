package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @oddjob.description Provides a component wrapper around a value type event source such as
 * {@link org.oddjob.state.expr.StateExpressionType}.
 *
 * @oddjob.example
 *
 * Running a job when two other jobs complete. Using components allows visibility on the individual event
 * sources in a UI.
 *
 * {@oddjob.xml.resource org/oddjob/events/ListSourceExample.xml}
 *
 *
 * @author Rob Gordon.
 *
 * @param <T> The type of the event.
 */
public class EventWatchComponent<T> extends EventSourceBase<T> {

    /**
     * @oddjob.property
     * @oddjob.description The event source being wrapped to be a component.
     * @oddjob.required Yes.
     */
    private EventSource<T> eventSource;

    @Override
    protected Restore doStart(Consumer<? super T> consumer) throws Exception {
        return Objects.requireNonNull(eventSource, "No Event Source")
                .subscribe(consumer);
    }

    public EventSource<T> getEventSource() {
        return eventSource;
    }

    public void setEventSource(EventSource<T> eventSource) {
        this.eventSource = eventSource;
    }
}
