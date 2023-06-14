package org.oddjob.state;


import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StateInstantTest {

    @Test
    void whenInstantTheSame() {

        List<Long> nanos = List.of(10L, 20L, 30L, 40L);
        Iterator<Long> it = nanos.iterator();

        StateInstant test = new StateInstant(
                Clock.fixed(Instant.ofEpochSecond(100L), ZoneOffset.UTC),
                it::next);

        Instant one = test._now();
        Instant two = test._now();
        Instant three = test._now();

        assertThat(one.toString(), is("1970-01-01T00:01:40.000000010Z"));
        assertThat(two.toString(), is("1970-01-01T00:01:40.000000020Z"));
        assertThat(three.toString(), is("1970-01-01T00:01:40.000000030Z"));
    }


    @Test
    void whenInstantAndNanosTheSame() {

        StateInstant test = new StateInstant(
                Clock.fixed(Instant.ofEpochSecond(100L), ZoneOffset.UTC),
                () -> 0L);

        Instant one = test._now();
        Instant two = test._now();
        Instant three = test._now();

        assertThat(one.toString(), is("1970-01-01T00:01:40.000000001Z"));
        assertThat(two.toString(), is("1970-01-01T00:01:40.000000002Z"));
        assertThat(three.toString(), is("1970-01-01T00:01:40.000000003Z"));
    }

    @Test
    void lotsOfRealClockCalls() throws ExecutionException, InterruptedException {

        ConcurrentLinkedQueue<Future<?>> futures = new ConcurrentLinkedQueue<>();

        LinkedList<Instant> results1 = new LinkedList<>();
        LinkedList<Instant> results2 = new LinkedList<>();

        int sample = 1_000;

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < sample; i++) {
            futures.add(executorService.submit(() -> {
                synchronized (results1) {
                    results1.add(StateInstant.now());
                }
            }));
        }
        for (int i = 0; i < sample; i++) {
            futures.add(executorService.submit(() -> {
                synchronized (results2) {
                    results2.add(StateInstant.now());
                }
            }));
        }

        for (Future<?> future: futures) {
            future.get();
        }

        executorService.shutdown();

        assertThat(results1.size(), is(sample));
        assertThat(results2.size(), is(sample));

        Set<Instant> ensureUnique = new HashSet<>();
        ensureUnique.addAll(results1);
        ensureUnique.addAll(results2);
        assertThat(ensureUnique.size(), is(sample * 2));
    }
}