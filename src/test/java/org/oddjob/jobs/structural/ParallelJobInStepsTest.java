package org.oddjob.jobs.structural;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.scheduling.MockScheduledExecutorService;
import org.oddjob.scheduling.MockScheduledFuture;
import org.oddjob.state.*;
import org.oddjob.tools.StateSteps;

import java.util.concurrent.Future;

public class ParallelJobInStepsTest extends OjTestCase {

    private static class ManualExecutor1 extends MockScheduledExecutorService {

        private Runnable runnable;

        @Override
        public Future<?> submit(Runnable runnable) {
            if (this.runnable != null) {
                throw new IllegalStateException();
            }
            this.runnable = runnable;
            return new MockScheduledFuture<Void>();
        }
    }

    @Test
    public void testStatesExecutingAndCompletingOneJob() {

        FlagState job1 = new FlagState(JobState.COMPLETE);

        ManualExecutor1 executor = new ManualExecutor1();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executor);

        test.setJobs(0, job1);

        test.run();

        steps.checkNow();

        assertNotNull(executor.runnable);

        steps.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        executor.runnable.run();

        steps.checkNow();

        steps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();
    }

    private static class ManualExecutor2 extends MockScheduledExecutorService {

        boolean cancelled;

        @Override
        public Future<?> submit(Runnable runnable) {
            return new MockScheduledFuture<Void>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    assertFalse(mayInterruptIfRunning);
                    cancelled = true;
                    return true;
                }
            };
        }
    }

    private static class CaptureStoppedJob extends SimpleJob
            implements Stoppable {

        @Override
        protected int execute() throws Throwable {
            throw new RuntimeException("Unexpected");
        }
    }


    @Test
    public void testStatesWhenStoppingJobThatHasntExecuted() throws FailedToStopException {

        CaptureStoppedJob job1 = new CaptureStoppedJob();

        ManualExecutor2 executor = new ManualExecutor2();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executor);

        test.setJobs(0, job1);

        test.run();

        steps.checkNow();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.READY);

        test.stop();

        steps.checkNow();

        assertFalse(job1.isStop());

        assertTrue(executor.cancelled);

        steps.startCheck(ParentState.READY, ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();
    }

    @Test
    public void testStatesWhenDestroyedWithoutStoppingJobThatHasntExecuted() {

        CaptureStoppedJob job1 = new CaptureStoppedJob();

        ManualExecutor2 executor = new ManualExecutor2();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executor);

        test.setJobs(0, job1);

        test.run();

        steps.checkNow();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.DESTROYED);

        test.destroy();

        assertFalse(job1.isStop());

        assertTrue(executor.cancelled);

        steps.checkNow();
    }

    private static class ManualExecutor3 extends MockScheduledExecutorService {

        boolean cancelled;

        Runnable runnable;

        public Future<?> submit(Runnable runnable) {
            this.runnable = runnable;
            return new MockScheduledFuture<Void>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    assertFalse(mayInterruptIfRunning);
                    cancelled = true;
                    return false;
                }
            };
        }
    }

    private static class AsyncJob implements Stateful, Runnable, Stoppable {

        private final JobStateHandler stateHandler =
                new JobStateHandler(this);

        private void sendEvent(final JobState jobState) {
            stateHandler.runLocked(() -> {
                stateHandler.setState(jobState);
                stateHandler.fireEvent();
            });
        }

        @Override
        public void run() {
            sendEvent(JobState.EXECUTING);
        }

        @Override
        public void addStateListener(StateListener listener)
                throws JobDestroyedException {
            stateHandler.addStateListener(listener);
        }

        @Override
        public void removeStateListener(StateListener listener) {
            stateHandler.removeStateListener(listener);
        }

        @Override
        public void stop() {
            sendEvent(JobState.COMPLETE);
        }

        @Override
        public StateEvent lastStateEvent() {
            return stateHandler.lastStateEvent();
        }
    }

    @Test
    public void testStatesWhenStoppingAsyncJobThatIsStillExecuting() throws FailedToStopException {

        AsyncJob job1 = new AsyncJob();

        ManualExecutor3 executor = new ManualExecutor3();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executor);

        test.setJobs(0, job1);

        test.run();

        executor.runnable.run();

        steps.checkNow();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.stop();

        steps.checkNow();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());

        assertTrue(executor.cancelled);

        steps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();
    }

    @Test
    public void testStatesWhenDestroyedWithoutStoppingAsyncJobThatIsStillExecuting() {

        AsyncJob job1 = new AsyncJob();

        ManualExecutor3 executor = new ManualExecutor3();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executor);

        test.setJobs(0, job1);

        test.run();

        executor.runnable.run();

        steps.checkNow();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();

        assertEquals(JobState.EXECUTING, job1.lastStateEvent().getState());

        assertTrue(executor.cancelled);

    }
}
