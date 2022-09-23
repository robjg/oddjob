package org.oddjob.jobs.structural;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oddjob.FailedToStopException;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.*;
import org.oddjob.tools.StateSteps;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

public class ParallelJobInStepsTest extends OjTestCase {

    @Test
    public void testStatesExecutingAndCompletingOneJob() {

        FlagState job1 = new FlagState(JobState.COMPLETE);

        ExecutorService executorService = mock(ExecutorService.class);

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executorService);

        test.setJobs(0, job1);

        test.run();

        steps.checkNow();

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService, times(1)).execute(runnableCaptor.capture());
        Runnable runnable = runnableCaptor.getValue();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        runnable.run();

        steps.checkNow();

        steps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();

        verifyNoMoreInteractions(executorService);
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

        ExecutorService executor = mock(ExecutorService.class);

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

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(runnableCaptor.capture());
        Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verifyNoMoreInteractions(executor);

        steps.startCheck(ParentState.READY, ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();
    }

    @Test
    public void testStatesWhenDestroyedWithoutStoppingJobThatHasntExecuted() {

        CaptureStoppedJob job1 = new CaptureStoppedJob();

        ExecutorService executor = mock(ExecutorService.class);

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

        verify(executor, times(1)).execute(any(Runnable.class));
        verifyNoMoreInteractions(executor);

        steps.checkNow();
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

        ExecutorService executorService = mock(ExecutorService.class);

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executorService);

        test.setJobs(0, job1);

        test.run();

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService, times(1)).execute(runnableCaptor.capture());
        Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        steps.checkNow();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.stop();

        steps.checkNow();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());

        steps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();

        verifyNoMoreInteractions(executorService);

    }

    @Test
    public void testStatesWhenDestroyedWithoutStoppingAsyncJobThatIsStillExecuting() {

        AsyncJob job1 = new AsyncJob();

        ExecutorService executorService = mock(ExecutorService.class);

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        test.setExecutorService(executorService);

        test.setJobs(0, job1);

        test.run();

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService, times(1)).execute(runnableCaptor.capture());
        Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        steps.checkNow();

        steps.startCheck(ParentState.ACTIVE,
                ParentState.DESTROYED);

        test.destroy();

        steps.checkNow();

        assertEquals(JobState.EXECUTING, job1.lastStateEvent().getState());

    }
}
