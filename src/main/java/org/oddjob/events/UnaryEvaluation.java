package org.oddjob.events;

import java.time.Instant;
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

    private final EventOf<T> operand;

    public UnaryEvaluation(T result, EventOf<T> operand) {
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

    public EventOf<T> getOperand() {
        return operand;
    }

    @Override
    public EventOf<T> getEvents(int index) {
        if (index < 0 || index > 0) {
            throw new IndexOutOfBoundsException("Only 0 allowed.");
        }
        return getOperand();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Stream<EventOf<? extends T>> stream() {
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
