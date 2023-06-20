package org.oddjob.state;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Ensure a unique instant for each state. Some jobs will not trigger if the time
 * of the state event hasn't changed. This ensures that each run of a Job will have
 * a unique event time so subsequent tasks run as expected.
 */
public class StateInstant implements Serializable {

    private static final long serialVersionUID = 2023061400L;

    private static final StateInstantClock system =
            StateInstantClock.fromClock(Clock.systemUTC(), System::nanoTime);

    private final Instant instant;

    private StateInstant(Instant instant) {
        this.instant = Objects.requireNonNull(instant);
    }

    public static StateInstant now(StateInstantClock clock) {
        return new StateInstant(clock.now());
    }

    public static StateInstant now() {
        return now(system);
    }

    public static StateInstant parse(String text) {
        return new StateInstant(Instant.parse(text));
    }

    /**
     * Required to support the deprecated constructors in {@link StateEvent}.
     *
     * @since 1.7
     *
     * @param instant The instant
     * @return The wrapped instant.
     */
    @Deprecated(since="1.7", forRemoval=true)
    public static StateInstant forOneVersionOnly(Instant instant) {
        return new StateInstant(instant);
    }

    public Instant getInstant() {
        return instant;
    }

    @Override
    public String toString() {
        return "StateInstant{" +
                "instant=" + instant +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateInstant that = (StateInstant) o;
        return instant.equals(that.instant);
    }

    @Override
    public int hashCode() {
        return instant.hashCode();
    }
}
