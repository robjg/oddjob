package org.oddjob.state;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * A clock that provides an {@link StateInstant}.
 */
abstract public class StateInstantClock {

    public abstract Instant now();

    public static StateInstantClock fromClock(Clock clock, LongSupplier nanoSupplier) {
        return new Default(clock, nanoSupplier);
    }

    static class Default extends StateInstantClock {


        private final Clock clock;

        private final LongSupplier nanoSupplier;

        private final AtomicReference<NowAndNano> nowAndNano;

        public Default(Clock clock, LongSupplier nanoSupplier) {
            this.clock = clock;
            this.nanoSupplier = nanoSupplier;
            nowAndNano = new AtomicReference<>(
                    new NowAndNano(clock.instant(), nanoSupplier.getAsLong()));
        }

        public Instant now() {

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
