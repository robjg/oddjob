package org.oddjob.state;


import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StateInstantTest {


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
                    results1.add(StateInstant.now().getInstant());
                }
            }));
        }
        for (int i = 0; i < sample; i++) {
            futures.add(executorService.submit(() -> {
                synchronized (results2) {
                    results2.add(StateInstant.now().getInstant());
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