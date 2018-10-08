package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PhasedWorkTest {

    private static class Work implements Runnable {

        private final AtomicBoolean complete = new AtomicBoolean();

        @Override
        public void run() {
            complete.set(true);
        }

        boolean isComplete() {
            return complete.get();
        }
    }

    @Test
    public void testSync() {
        testWithExecutor(Runnable::run);
    }

    @Test
    public void testAsync() {

        ExecutorService es = Executors.newFixedThreadPool(2);

        testWithExecutor(es);

        es.shutdown();
    }

    void testWithExecutor(Executor executor) {

        Work complete = new Work();

        PhasedWork test = new PhasedWork(complete,
                executor);

        Work work1 = new Work();
        Work work2 = new Work();

        test.execute(work1);
        test.execute(work2);

        CompletableFuture<?> f = test.complete();
        f.join();

        assertThat(work1.isComplete(), is(true));
        assertThat(work2.isComplete(), is(true));
        assertThat(complete.isComplete(), is(true));
    }
}
