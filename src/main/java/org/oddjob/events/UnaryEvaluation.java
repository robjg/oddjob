package org.oddjob.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The result of applying a Operation to an event of some type that results
 * in a new event of the same type.
 *
 * @param <T> The Type of the event.
 */
public class UnaryEvaluation<T> implements CompositeEvent<T> {

    private final T result;

    private final Instant lastTime;

    private final InstantEvent<T> operand;

    public UnaryEvaluation(T result, InstantEvent<T> operand) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(operand);

        this.result = result;
        this.lastTime = operand.getTime();
        this.operand = operand;
    }

    @Override
    public T getOf() {
        return result;
    }

    public InstantEvent<T> getOperand() {
        return operand;
    }

    @Override
    public InstantEvent<T> getEvents(int index) {
        if (index < 0 || index > 0) {
            throw new IndexOutOfBoundsException("Only 0 allowed.");
        }
        return getOperand();
    }

    @Override
    public int getCount() {
        return 1;
    }

    public List<T> getOfs() {
        return EventConversions.toList(this);
    }

    @Override
    public Stream<InstantEvent<? extends T>> stream() {
        return Stream.of(operand);
    }

    @Override
    public Instant getTime() {
            return lastTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryEvaluation that = (UnaryEvaluation) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(lastTime, that.lastTime) &&
                Objects.equals(operand, that.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, lastTime, operand);
    }

    @Override
    public String toString() {
        return "UnaryEvaluation{" +
                "result=" + result +
                ", lastTime=" + lastTime +
                ", operand=" + operand +
                '}';
    }
}
