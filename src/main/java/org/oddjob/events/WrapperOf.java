package org.oddjob.events;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * An event of something create by wrapping it.
 *
 * @param <T> The type the event is of.
 */
public class WrapperOf<T> implements InstantEvent<T> {

    private final T event;

    private final Instant time;

    public WrapperOf(T event, Date date) {
        this(event, date.toInstant());
    }

    public WrapperOf(T event, Instant time) {
        this.event = event;
        this.time = time;
    }

    @Override
    public T getOf() {
        return event;
    }

    @Override
    public Instant getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrapperOf<?> that = (WrapperOf<?>) o;
        return Objects.equals(event, that.event) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, time);
    }

    @Override
    public String toString() {
        return "WrapperOf{" +
                "event=" + event +
                ", time=" + time +
                '}';
    }
}
