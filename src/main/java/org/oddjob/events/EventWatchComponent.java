package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.Objects;
import java.util.function.Consumer;

public class EventWatchComponent<T> extends EventSourceBase<T> {

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
