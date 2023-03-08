package org.oddjob.scheduling;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.CapturingMatcher;
import org.oddjob.FailedToStopException;
import org.oddjob.OjTestCase;
import org.oddjob.Stoppable;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.schedules.*;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ManualClock;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TimerStopTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(TimerStopTest.class);

    @Before
    public void setUp() throws Exception {
        logger.info("----------------  " + getName() + "  ---------------");
    }

    private static class NeverRan extends SimpleJob {
        @Override
        protected int execute() throws Throwable {
            throw new RuntimeException("Unexpected.");
        }
    }

    @Test
    public void testStopBeforeRunning() throws FailedToStopException {
        DefaultExecutors services = new DefaultExecutors();

        Timer test = new Timer();
        test.setScheduleExecutorService(
                services.getScheduledExecutor());
        test.setSchedule(context -> new IntervalTo(Interval.END_OF_TIME));

        NeverRan job = new NeverRan();

        test.setJob(job);

        test.run();

        test.stop();

        assertEquals(JobState.READY,
                job.lastStateEvent().getState());

        assertEquals(TimerState.STARTABLE,
                test.lastStateEvent().getState());

        test.force();

        assertEquals(TimerState.COMPLETE,
                test.lastStateEvent().getState());

        services.stop();


    }

    private static class RunningJob extends SimpleJob
            implements Stoppable {

        final Exchanger<Void> running = new Exchanger<>();

        final AtomicReference<Thread> threadRef = new AtomicReference<>();

        @Override
        protected int execute() throws Throwable {
            threadRef.set(Thread.currentThread());
            running.exchange(null);
            synchronized (this) {
                try {
                    logger.info("Running job waiting.");
                    wait();
                } catch (InterruptedException e) {
                    logger.info("Running job interrupted.");
                }
            }
            return 0;
        }

        @Override
        protected void onStop() {
            Optional.ofNullable(threadRef.get())
                    .orElseThrow(() -> new IllegalStateException(
                            "Should be impossible for thread not to have been set."))
                    .interrupt();
        }
    }

    @Test
    public void testStopWhenRunning() throws InterruptedException, FailedToStopException {
        DefaultExecutors services = new DefaultExecutors();

        Timer test = new Timer();
        test.setScheduleExecutorService(
                services.getScheduledExecutor());
        test.setSchedule(context -> new IntervalTo(new Date()));

        RunningJob job = new RunningJob();

        test.setJob(job);

        StateSteps jobState = new StateSteps(job);
        jobState.startCheck(JobState.READY, JobState.EXECUTING);

        test.run();

        job.running.exchange(null);

        jobState.checkNow();
        jobState.startCheck(JobState.EXECUTING, JobState.COMPLETE);

        test.stop();

        assertFalse(Thread.interrupted());

        assertEquals(TimerState.STARTABLE,
                test.lastStateEvent().getState());

        jobState.checkWait();

        services.stop();
    }

    private static class RunOnceJob extends SimpleJob {

        boolean ran;

        @Override
        protected int execute() throws Throwable {
            if (ran) {
                throw new Exception("Unexpected.");
            }
            return 0;
        }
    }

    @Test
    public void testStopBetweenSchedules() throws Throwable {
        DefaultExecutors services = new DefaultExecutors();
        services.setPoolSize(5);

        final Timer test = new Timer();
        test.setScheduleExecutorService(
                services.getScheduledExecutor());
        test.setSchedule(context -> {
            if (context.getData("done") == null) {
                context.putData("done", new Object());
                return new IntervalTo(new Date());
            } else {
                return new IntervalTo(Interval.END_OF_TIME);
            }
        });

        RunOnceJob job = new RunOnceJob();
        test.setJob(job);

        StateSteps testStates = new StateSteps(test);

        testStates.startCheck(TimerState.STARTABLE,
                TimerState.STARTING, TimerState.ACTIVE,
                TimerState.STARTED);

        logger.info("** First Run **");

        test.run();

        testStates.checkWait();

        testStates.startCheck(TimerState.STARTED,
                TimerState.STARTABLE);

        test.stop();

        testStates.checkWait();

        assertEquals(JobState.COMPLETE,
                job.lastStateEvent().getState());

        testStates.startCheck(TimerState.STARTABLE,
                TimerState.STARTING, TimerState.STARTED);

        logger.info("** Second Run **");

        test.run();

        testStates.checkNow();

        assertEquals(new IntervalTo(Interval.END_OF_TIME), test.getCurrent());

        test.stop();

        services.stop();
    }

    /**
     * Tracking down a bug with MissedSkipped when stop and started in
     * the current interval re-runs the current interval.
     *
     * @throws FailedToStopException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testStopBetweenScheduleWithSkippedRuns() throws FailedToStopException {

        final ScheduledFuture<?> future = Mockito.mock(ScheduledFuture.class);

        ScheduledExecutorService executor =
                Mockito.mock(ScheduledExecutorService.class);

        CapturingMatcher<Runnable> runnable =
                new CapturingMatcher<>(Runnable.class);

        CapturingMatcher<Long> delay = new CapturingMatcher<>(Long.class);

        Mockito.when(executor.schedule(
                Mockito.argThat(runnable),
                Mockito.longThat(delay),
                Mockito.eq(TimeUnit.MILLISECONDS))).thenReturn(
                (ScheduledFuture) future);

        Timer test = new Timer();
        test.setSchedule(new DailySchedule());
        test.setClock(new ManualClock("2012-01-24 01:00"));
        test.setScheduleExecutorService(executor);
        test.setJob(new RunOnceJob());
        test.setSkipMissedRuns(true);

        test.run();

        assertEquals(TimerState.ACTIVE, test.lastStateEvent().getState());

        ScheduleResult expectedCurrent1 = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2012-01-24 00:00"),
                        DateHelper.parseDateTime("2012-01-25 00:00")));

        assertEquals(Long.valueOf(0), delay.getLastValue());
        assertEquals(expectedCurrent1, test.getCurrent());

        runnable.getLastValue().run();

        ScheduleResult expectedCurrent2 = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2012-01-25 00:00"),
                        DateHelper.parseDateTime("2012-01-26 00:00")));

        assertEquals(expectedCurrent2, test.getCurrent());
        assertEquals(Long.valueOf(23 * 60 * 60 * 1000L), delay.getLastValue());

        test.stop();

        assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());

        test.run();

        assertEquals(TimerState.STARTED, test.lastStateEvent().getState());

        assertEquals(expectedCurrent2, test.getCurrent());
        assertEquals(Long.valueOf(23 * 60 * 60 * 1000L), delay.getLastValue());

        test.stop();

        assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());

        test.setJob(null);
        test.setJob(new RunOnceJob());
        test.setClock(new ManualClock("2012-01-28 01:00"));

        test.run();

        assertEquals(TimerState.ACTIVE, test.lastStateEvent().getState());

        ScheduleResult expectedCurrent3 = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2012-01-28 00:00"),
                        DateHelper.parseDateTime("2012-01-29 00:00")));

        assertEquals(Long.valueOf(0), delay.getLastValue());
        assertEquals(expectedCurrent3, test.getCurrent());

        runnable.getLastValue().run();

        ScheduleResult expectedCurrent4 = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2012-01-29 00:00"),
                        DateHelper.parseDateTime("2012-01-30 00:00")));

        assertEquals(expectedCurrent4, test.getCurrent());
        assertEquals(Long.valueOf(23 * 60 * 60 * 1000L), delay.getLastValue());

        test.stop();

        assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());

    }

    /**
     * This is what happens when a Retry or an unfinished sequential
     * is stopped.
     *
     * @throws FailedToStopException
     * @throws InterruptedException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testWhenScheduledChildGoesToReady() throws FailedToStopException {

        final ScheduledFuture<?> future = Mockito.mock(ScheduledFuture.class);

        ScheduledExecutorService executor =
                Mockito.mock(ScheduledExecutorService.class);

        CapturingMatcher<Runnable> runnable =
                new CapturingMatcher<>(Runnable.class);

        CapturingMatcher<Long> delay = new CapturingMatcher<>(Long.class);

        Mockito.when(executor.schedule(
                Mockito.argThat(runnable),
                Mockito.longThat(delay),
                Mockito.eq(TimeUnit.MILLISECONDS))).thenReturn(
                (ScheduledFuture) future);

        Timer test = new Timer();
        test.setSchedule(new DailySchedule());
        test.setClock(new ManualClock("2012-07-11 01:00"));
        test.setScheduleExecutorService(executor);

        SequentialJob child = new SequentialJob();
        test.setJob(child);

        test.run();

        Mockito.verify(executor).schedule(
                Mockito.argThat(runnable),
                Mockito.longThat(delay),
                Mockito.eq(TimeUnit.MILLISECONDS));

        assertEquals(TimerState.ACTIVE, test.lastStateEvent().getState());

        ScheduleResult expectedCurrent1 = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2012-07-11 00:00"),
                        DateHelper.parseDateTime("2012-07-12 00:00")));

        assertEquals(Long.valueOf(0), delay.getLastValue());
        assertEquals(expectedCurrent1, test.getCurrent());

        runnable.getLastValue().run();

        Mockito.verifyNoMoreInteractions(executor);

        assertEquals(ParentState.READY, child.lastStateEvent().getState());

        FlagState flag = new FlagState();

        child.setJobs(0, flag);
        flag.run();

        assertEquals(ParentState.COMPLETE, child.lastStateEvent().getState());

        Mockito.verify(executor, Mockito.times(2)).schedule(
                Mockito.argThat(runnable),
                Mockito.longThat(delay),
                Mockito.eq(TimeUnit.MILLISECONDS));

        ScheduleResult expectedCurrent2 = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2012-07-12 00:00"),
                        DateHelper.parseDateTime("2012-07-13 00:00")));

        assertEquals(expectedCurrent2, test.getCurrent());
        assertEquals(Long.valueOf(23 * 60 * 60 * 1000L), delay.getLastValue());

        test.stop();

        assertEquals(TimerState.STARTABLE, test.lastStateEvent().getState());
    }
}
