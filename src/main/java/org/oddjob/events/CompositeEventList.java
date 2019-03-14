package org.oddjob.events;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * An {@link CompositeEvent} created from some other events. The event of this event is youngest event of the
 * composites. The time of this event is that of the youngest event.
 *
 * @param <T> The type of the events being combined.
 */
public class CompositeEventList<T> implements CompositeEvent<T> {

    private final List<EventOf<? extends T>> events;

    private final EventOf<? extends T> last;

    public CompositeEventList(EventOf<? extends T>... events) {
        this(Arrays.asList(events));
    }

    CompositeEventList(List<EventOf<? extends T>> events) {
        this.events = events;
        this.last = findLast(events);
    }

    private static <T> EventOf<? extends T> findLast(Collection<EventOf<? extends T>> events) {
        return events.stream()
                     .max(Comparator.comparing(e -> e.getTime()))
                     .orElse(null);
    }

    @Override
    public T getOf() {
        if (last == null) {
            return null;
        }
        else {
            return last.getOf();
        }
    }

    @Override
    public EventOf<? extends T> getEvents(int index) {
        return events.get(index);
    }

    @Override
    public Instant getTime() {
        if (last == null) {
            return Instant.MIN;
        }
        else {
            return last.getTime();
        }
    }

    @Override
    public int getCount() {
        return events.size();
    }

    public List<T> getOfs() {
        return EventConversions.toList(this);
    }

    @Override
    public Stream<EventOf<? extends T>> stream() {
        return events.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeEventList<?> that = (CompositeEventList<?>) o;
        return Objects.equals(events, that.events) &&
                Objects.equals(last, that.last);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events, last);
    }

    @Override
    public String toString() {
        return "CompositeEventList{" +
                "events=" + events +
                ", last=" + last +
                '}';
    }
}
