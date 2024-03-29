package org.oddjob.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The result of applying a Binary Operation to some events of some type that results
 * in a new event of the same type.
 *
 * @param <T> The Type of the events.
 */
public class BinaryEvaluation<T> implements CompositeEvent<T> {

    private final T result;

    private final Instant lastTime;

    private final InstantEvent<T> lhs;

    private final InstantEvent<T> rhs;

    public BinaryEvaluation(T result, InstantEvent<T> lhs, InstantEvent<T> rhs) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(lhs);
        Objects.requireNonNull(rhs);

        this.result = result;
        this.lhs = lhs;
        this.rhs = rhs;
        this.lastTime = Stream.of(lhs.getTime(), rhs.getTime())
                              .max(Instant::compareTo)
                              .orElseThrow(() -> new IllegalArgumentException(
                                      "This will never happen."));
    }

    @Override
    public T getOf() {
        return result;
    }

    public InstantEvent<T> getLhs() {
        return lhs;
    }

    public InstantEvent<T> getRhs() {
        return rhs;
    }

    @Override
    public InstantEvent<T> getEvents(int index) {
        if (index == 0) {
            return getLhs();
        }
        else if (index ==1) {
            return getRhs();
        }
        else {
            throw new IndexOutOfBoundsException(
                    String.format("Index must be 0 or 1, not %d",
                                  index));
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public List<T> getOfs() {
        return EventConversions.toList(this);
    }

    @Override
    public Stream<InstantEvent<? extends T>> stream() {
        return Stream.of(lhs, rhs);
    }

    @Override
    public Instant getTime() {
        return lastTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryEvaluation that = (BinaryEvaluation) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(lastTime, that.lastTime) &&
                Objects.equals(lhs, that.lhs) &&
                Objects.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, lastTime, lhs, rhs);
    }

    @Override
    public String toString() {
        return "BinaryEvaluation{" +
                "result=" + result +
                ", lastTime=" + lastTime +
                ", lhs=" + lhs +
                ", rhs=" + rhs +
                '}';
    }
}
