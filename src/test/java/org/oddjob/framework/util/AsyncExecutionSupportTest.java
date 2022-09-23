package org.oddjob.framework.util;

import org.junit.Test;
import org.oddjob.OjTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class AsyncExecutionSupportTest extends OjTestCase {

    @Test
    public void testAddTwoJobs() {

        List<Runnable> submitted = new ArrayList<>();

        Executor executor = mock(Executor.class);
        doAnswer(invocationOnMock -> {
            submitted.add(invocationOnMock.getArgument(0));
            return null;
        }).when(executor).execute(any(Runnable.class));

        final AtomicBoolean done = new AtomicBoolean();

        AsyncExecutionSupport test = new AsyncExecutionSupport(
                () -> done.set(true),
                t -> { throw new RuntimeException(t); });

        test.submitJob(executor, () -> {
        });

        test.submitJob(executor, () -> {
        });

        test.startWatchingJobs();

        assertFalse(done.get());

        submitted.get(1).run();

        assertFalse(done.get());

        submitted.get(0).run();

        assertTrue(done.get());
    }

    @Test
    public void testStopBeforeAnyJobsSubmitted() {

        final AtomicBoolean done = new AtomicBoolean();

        AsyncExecutionSupport test = new AsyncExecutionSupport(
                () -> done.set(true),
                t -> { throw new RuntimeException(t); });

        test.startWatchingJobs();

        assertTrue("done=" + done.get(), done.get());
    }
}
