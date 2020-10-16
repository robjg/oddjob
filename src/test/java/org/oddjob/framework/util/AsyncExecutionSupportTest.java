package org.oddjob.framework.util;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.scheduling.MockExecutorService;
import org.oddjob.scheduling.MockFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncExecutionSupportTest extends OjTestCase {

    private static class OurExecutor extends MockExecutorService {

        List<Runnable> submitted = new ArrayList<>();

        @Override
        public Future<?> submit(Runnable task) {
            submitted.add(task);
            return new MockFuture<Object>() {
            };
        }
    }


    @Test
    public void testAddTwoJobs() {

        OurExecutor executor = new OurExecutor();

        final AtomicBoolean done = new AtomicBoolean();

        AsyncExecutionSupport test = new AsyncExecutionSupport(() -> done.set(true));

        test.submitJob(executor, () -> {
        });

        test.submitJob(executor, () -> {
        });

        test.startWatchingJobs();

        assertFalse(done.get());

        executor.submitted.get(1).run();

        assertFalse(done.get());

        executor.submitted.get(0).run();

        assertTrue(done.get());
    }

    @Test
    public void testStopBeforeAnyJobsSubmitted() {

        final AtomicBoolean done = new AtomicBoolean();

        AsyncExecutionSupport test = new AsyncExecutionSupport(() -> done.set(true));

        test.startWatchingJobs();

        assertTrue("done=" + done.get(), done.get());
    }
}
