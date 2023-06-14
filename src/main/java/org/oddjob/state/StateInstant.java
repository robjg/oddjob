package org.oddjob.state;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Ensure a unique instant for each state. Some jobs will not trigger if the time
 * of the state event hasn't changed. This ensures that each run of a Job will have
 * a unique event time so subsequent tasks run as expected.
 */
public class StateInstant {

    private static final StateInstant system =
            new StateInstant(Clock.systemUTC(), System::nanoTime);
    private final Clock clock;

    private final LongSupplier nanoSupplier;

    private final AtomicReference<NowAndNano> nowAndNano;

    public StateInstant(Clock clock, LongSupplier nanoSupplier) {
        this.clock = clock;
        this.nanoSupplier = nanoSupplier;
        nowAndNano = new AtomicReference<>(
                new NowAndNano(clock.instant(), nanoSupplier.getAsLong()));
    }

    public static Instant now() {
        return system._now();
    }

    public Instant _now() {

        while (true) {

            NowAndNano last = nowAndNano.get();
            Instant now = clock.instant();
            long nano = nanoSupplier.getAsLong();
            if (now.compareTo(last.now) <= 0) {
                long elapsed = nano - last.nano;
                if (elapsed <= 0) {
                    elapsed = 1;
                }
                now = Instant.ofEpochSecond(last.now.getEpochSecond(),
                        elapsed + (long) last.now.getNano());
            }
            NowAndNano again = new NowAndNano(now, nano);
            if (nowAndNano.compareAndSet(last, again)) {
                return now;
            }
        }
    }

    static class NowAndNano {

        private final Instant now;

        private final long nano;

        NowAndNano(Instant now, long nano) {
            this.now = now;
            this.nano = nano;
        }
    }
}
