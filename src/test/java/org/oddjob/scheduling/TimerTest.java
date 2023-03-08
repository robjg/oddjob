/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.WaitJob;
import org.oddjob.persist.MapPersister;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;
import org.oddjob.schedules.schedules.*;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.*;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.ManualClock;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class TimerTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TimerTest.class);

    @Before
    public void setUp() {
        logger.debug("=============== " + getName() + " ===================");
    }

    private static class OurJob extends MockStateful
            implements Runnable, Resettable {

        int resets;

        final List<StateListener> listeners = new ArrayList<>();

        public void addStateListener(StateListener listener) {
            listeners.add(listener);
            listener.jobStateChange(new StateEvent(this, JobState.READY));

        }

        public void removeStateListener(StateListener listener) {
            listeners.remove(listener);
        }


        public void run() {
            List<StateListener> copy = new ArrayList<>(listeners);
            for (StateListener listener : copy) {
                listener.jobStateChange(new StateEvent(this, JobState.EXECUTING));
                listener.jobStateChange(new StateEvent(this, JobState.COMPLETE));
            }
        }

        public boolean hardReset() {
            ++resets;
            return true;
        }

        public boolean softReset() {
            throw new RuntimeException("Unexpected.");
        }
    }

    private static class OurScheduledExecutorService extends MockScheduledExecutorService {

        Runnable runnable;
        long delay;

        ScheduledFuture<Void> future = new MockScheduledFuture<>();

        public ScheduledFuture<?> schedule(Runnable runnable, long delay,
                                           TimeUnit unit) {

            OurScheduledExecutorService.this.delay = delay;
            OurScheduledExecutorService.this.runnable = runnable;

            return future;
        }
    }

    @Test
    public void testSimpleNonRepeatingSchedule()
            throws Exception {

        DateSchedule schedule = new DateSchedule();
        schedule.setOn("2020-12-25");

        OurJob ourJob = new OurJob();

        ManualClock clock = new ManualClock("2020-12-24");

        Timer test = new Timer();
        test.setSchedule(schedule);
        test.setJob(ourJob);
        test.setHaltOnFailure(true);
        test.setClock(clock);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();
        test.setScheduleExecutorService(oddjobServices);

        StateSteps timerStates = new StateSteps(test);
        timerStates.startCheck(TimerState.STARTABLE, TimerState.STARTING,
                TimerState.STARTED);
        IconSteps timerIcons = new IconSteps(test);
        timerIcons.startCheck(IconHelper.STARTABLE, IconHelper.EXECUTING,
                IconHelper.STARTED);

        test.run();

        timerStates.checkNow();
        timerIcons.checkNow();

        Date expected = DateHelper.parseDate("2020-12-25");

        assertEquals(expected, test.getNextDue());
        assertEquals(expected, test.getCurrent().getFromDate());
        assertEquals(24 * 60 * 60 * 1000L, oddjobServices.delay);

        timerStates.startCheck(TimerState.STARTED, TimerState.ACTIVE,
                TimerState.COMPLETE);
        timerIcons.startCheck(IconHelper.STARTED, IconHelper.ACTIVE,
                IconHelper.COMPLETE);

        // Time passes... Executor runs job.
        oddjobServices.delay = -1;
        oddjobServices.runnable.run();
        oddjobServices.runnable = null;

        assertNull(null, test.getNextDue());
        assertNull(null, test.getCurrent());
        assertEquals(expected, test.getLastDue());
        assertEquals(-1, oddjobServices.delay);

        assertEquals(1, ourJob.resets);
        timerStates.checkNow();
        timerIcons.checkNow();

        //
        // Check reset and run again works as expected.

        clock.setDateText("2020-12-25 00:00:01");

        timerStates.startCheck(TimerState.COMPLETE, TimerState.STARTABLE);
        timerIcons.startCheck(IconHelper.COMPLETE, IconHelper.STARTABLE);

        test.hardReset();

        timerStates.checkNow();
        timerIcons.checkNow();

        timerStates.startCheck(TimerState.STARTABLE,
                TimerState.STARTING, TimerState.ACTIVE);
        timerIcons.startCheck(IconHelper.STARTABLE, IconHelper.EXECUTING,
                IconHelper.ACTIVE);

        test.run();

        timerStates.checkNow();
        timerIcons.checkNow();

        assertEquals(expected, test.getNextDue());
        assertEquals(expected, test.getCurrent().getFromDate());
        assertEquals(0L, oddjobServices.delay);

        timerStates.startCheck(TimerState.ACTIVE,
                TimerState.COMPLETE);
        timerIcons.startCheck(IconHelper.ACTIVE,
                IconHelper.COMPLETE);

        // Time has passed... Executor would run job immediately.
        oddjobServices.delay = -1;
        oddjobServices.runnable.run();
        oddjobServices.runnable = null;

        assertNull(null, test.getNextDue());
        assertNull(null, test.getCurrent());
        assertEquals(expected, test.getLastDue());
        assertEquals(-1, oddjobServices.delay);

        assertEquals(3, ourJob.resets);
        timerStates.checkNow();
        timerIcons.checkNow();


        //
        // Destroy
        test.setJob(null);

        test.destroy();

        assertEquals(0, ourJob.listeners.size());
    }

    @Test
    public void testRecurringScheduleWhenStopped() {

        FlagState job = new FlagState();
        job.setState(JobState.COMPLETE);

        DailySchedule time = new DailySchedule();
        time.setFrom("14:45");
        time.setTo("14:55");

        ManualClock clock = new ManualClock("2009-02-10 14:50");

        Timer test = new Timer();
        test.setSchedule(time);
        test.setClock(clock);
        test.setJob(job);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertNotNull(oddjobServices.runnable);
        assertEquals(0, oddjobServices.delay);

        oddjobServices.runnable.run();

        Date expectedNextDue = DateHelper.parseDateTime(
                "2009-02-11 14:45");

        assertEquals(expectedNextDue, test.getNextDue());

        assertEquals(expectedNextDue.getTime() - clock.getDate().getTime(),
                oddjobServices.delay);
    }

    @Test
    public void testOverdueSchedule() {

        FlagState job = new FlagState();
        job.setState(JobState.COMPLETE);

        DailySchedule time = new DailySchedule();
        time.setAt("12:00");

        ManualClock clock = new ManualClock("2009-03-02 14:00");

        Timer test = new Timer();
        test.setSchedule(time);
        test.setClock(clock);
        test.setJob(job);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertNotNull(oddjobServices.runnable);
        assertEquals(22 * 60 * 60 * 1000, oddjobServices.delay);

        // simulate job longer than next due;
        clock.setDateText("2009-03-04 13:00");
        oddjobServices.runnable.run();

        assertEquals(0, oddjobServices.delay);

        // next one runs quick.
        clock.setDateText("2009-03-04 18:00");
        oddjobServices.runnable.run();

        assertEquals(18 * 60 * 60 * 1000, oddjobServices.delay);
    }

    @Test
    public void testSkipMissedSchedule() {

        FlagState job = new FlagState();
        job.setState(JobState.COMPLETE);

        DailySchedule time = new DailySchedule();
        time.setAt("12:00");

        ManualClock clock = new ManualClock("2009-03-02 14:00");

        Timer test = new Timer();
        test.setSchedule(time);
        test.setClock(clock);
        test.setJob(job);
        test.setSkipMissedRuns(true);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertNotNull(oddjobServices.runnable);
        assertEquals(22 * 60 * 60 * 1000, oddjobServices.delay);

        // simulate job longer than next due;
        clock.setDateText("2009-03-04 13:00");
        oddjobServices.runnable.run();

        // next one runs the next day.
        assertEquals(23 * 60 * 60 * 1000, oddjobServices.delay);
    }

    @Test
    public void testHaltOnFailure() {

        FlagState job = new FlagState();
        job.setState(JobState.INCOMPLETE);

        TimeSchedule time = new TimeSchedule();
        time.setFrom("14:45");
        time.setTo("14:55");

        ManualClock clock = new ManualClock("2009-02-10 14:50");

        Timer test = new Timer();
        test.setSchedule(time);
        test.setClock(clock);
        test.setHaltOnFailure(true);
        test.setJob(job);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertNotNull(oddjobServices.runnable);
        assertEquals(0, oddjobServices.delay);

        oddjobServices.delay = -1;

        oddjobServices.runnable.run();

        assertEquals(-1, oddjobServices.delay);
        assertEquals(null, test.getNextDue());

        assertEquals(TimerState.INCOMPLETE, test.lastStateEvent().getState());
    }

    @Test
    public void testTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        DateSchedule schedule = new DateSchedule();
        schedule.setOn("2030-06-21");
        DailySchedule daily = new DailySchedule();
        daily.setAt("10:00");
        schedule.setRefinement(daily);

        OurJob ourJob = new OurJob();

        Timer test = new Timer();
        test.setSchedule(schedule);
        test.setJob(ourJob);
        test.setTimeZone("GMT+8");

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertEquals(DateHelper.parseDateTime("2030-06-21 02:00"),
                test.getNextDue());

        TimeZone.setDefault(null);
    }

    @Test
    public void testSerialize() throws Exception {

        FlagState sample = new FlagState();
        sample.setState(JobState.COMPLETE);

        Timer test = new Timer();

        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:00:05");
        CountSchedule count = new CountSchedule();
        count.setCount(2);
        count.setRefinement(interval);

        ManualClock clock = new ManualClock("2009-02-10 14:30");

        test.setSchedule(count);
        test.setJob(sample);
        test.setClock(clock);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertEquals(0, oddjobServices.delay);

        oddjobServices.runnable.run();

        assertEquals(5000, oddjobServices.delay);

        Timer copy = OddjobTestHelper.copy(test);

        copy.setClock(clock);
        copy.setScheduleExecutorService(oddjobServices);

        assertEquals(5000, oddjobServices.delay);

        Runnable runnable = oddjobServices.runnable;

        oddjobServices.runnable = null;

        runnable.run();

        assertNull(copy.getNextDue());
        assertNull(oddjobServices.runnable);

    }

    @Test
    public void testSerializeNotComplete() throws Exception {

        FlagState sample = new FlagState();
        sample.setState(JobState.INCOMPLETE);

        Timer test = new Timer();

        ManualClock clock = new ManualClock("2009-02-10 14:30");

        test.setSchedule(new NowSchedule());
        test.setJob(sample);
        test.setClock(clock);

        OurScheduledExecutorService oddjobServices = new OurScheduledExecutorService();

        test.setScheduleExecutorService(oddjobServices);

        test.run();

        assertEquals(0, oddjobServices.delay);

        oddjobServices.runnable.run();

        Timer copy = OddjobTestHelper.copy(test);

        assertEquals(TimerState.STARTABLE, copy.lastStateEvent().getState());

        assertEquals(DateHelper.parseDateTime("2009-02-10 14:30"),
                test.getLastDue());

        Interval expectedInterval = new SimpleInterval(
                DateHelper.parseDateTime("2009-02-10 14:30"));

        assertEquals(new SimpleScheduleResult(
                        expectedInterval, expectedInterval.getFromDate()),
                test.getCurrent());

    }

    // Stub services for Stop test.
    private static class StubExecutorServicesForTestStop extends MockScheduledExecutorService {

        boolean cancelled;

        public ScheduledFuture<?> schedule(Runnable runnable, long delay,
                                           TimeUnit unit) {

            if (delay < 1) {
                logger.info("** Service Executing [" + runnable + "] (" +
                        runnable.getClass().getName() + ")");
                new Thread(runnable).start();
            } else {
                logger.info("** Delay is [" + delay +
                        "], will never execute [" + runnable + "]");
            }

            return new MockScheduledFuture<Void>() {
                public boolean cancel(boolean interrupt) {
                    cancelled = true;
                    return false;
                }
            };
        }
    }

    private static class ToggleJobs extends SimpleJob {
        final AtomicInteger i = new AtomicInteger();
        final Runnable[] jobs = {
                new FlagState(), new WaitJob()
        };

        @Override
        protected int execute() throws Throwable {
            logger.info("Running job [" + i.get() + "]");
            jobs[i.getAndIncrement()].run();
            return 0;
        }
    }

    @Test
    public void testStopWhileANestedRetryJobIsExecutingASecondTime()
            throws ParseException, InterruptedException, FailedToStopException {

        final Timer test = new Timer();
        test.setSchedule(new CountSchedule(2));

        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:15");

        Retry retry = new Retry();
        retry.setSchedule(interval);

        ToggleJobs child = new ToggleJobs();

        retry.setJob(child);

        test.setJob(retry);

        StubExecutorServicesForTestStop services = new StubExecutorServicesForTestStop();

        test.setScheduleExecutorService(services);
        retry.setScheduleExecutorService(services);

        StateSteps timerStates = new StateSteps(test);
        timerStates.startCheck(TimerState.STARTABLE, TimerState.STARTING,
                TimerState.ACTIVE);

        StateSteps childStates = new StateSteps(child);
        childStates.startCheck(JobState.READY, JobState.EXECUTING,
                JobState.COMPLETE, JobState.READY, JobState.EXECUTING);

        logger.info("** Starting timer.");
        test.run();

        childStates.checkWait();
        timerStates.checkNow();

        timerStates.startCheck(TimerState.ACTIVE,
                TimerState.STARTABLE);

        logger.info("** Stopping timer.");
        test.stop();

        timerStates.checkWait();

        assertEquals(true, services.cancelled);

        test.setJob(null);
        retry.destroy();
        test.destroy();

    }

    @Test
    public void testStopBeforeTriggered() throws FailedToStopException {

        class Executor extends MockScheduledExecutorService {

            boolean canceled;
            Runnable job;

            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay,
                                               TimeUnit unit) {
                job = command;
                return new MockScheduledFuture<Void>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        assertEquals(false, mayInterruptIfRunning);
                        canceled = true;
                        job = null;
                        return true;
                    }
                };
            }
        }
        Executor executor = new Executor();

        Timer test = new Timer();
        test.setClock(new ManualClock("2011-09-30 00:10"));
        test.setScheduleExecutorService(executor);

        test.setJob(new FlagState());

        TimeSchedule schedule = new TimeSchedule();
        test.setSchedule(schedule);

        StateSteps timerStates = new StateSteps(test);
        timerStates.startCheck(TimerState.STARTABLE, TimerState.STARTING,
                TimerState.ACTIVE);
        IconSteps timerIcons = new IconSteps(test);
        timerIcons.startCheck(IconHelper.STARTABLE, IconHelper.EXECUTING,
                IconHelper.ACTIVE);

        test.run();

        timerStates.checkNow();
        timerIcons.checkNow();

        timerStates.startCheck(TimerState.ACTIVE, TimerState.STARTABLE);
        timerIcons.startCheck(IconHelper.ACTIVE, IconHelper.STOPPING,
                IconHelper.STARTABLE);

        test.stop();

        timerStates.checkNow();
        timerIcons.checkNow();

        assertEquals(true, executor.canceled);

        timerStates.startCheck(TimerState.STARTABLE, TimerState.STARTING,
                TimerState.ACTIVE);

        test.run();

        timerStates.checkNow();

        timerStates.startCheck(TimerState.ACTIVE, TimerState.COMPLETE);

        executor.job.run();

        timerStates.checkNow();

        test.destroy();

    }

    /**
     * Schedule doesn't have to be serializable.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void testSerializeUnserializbleSchedule() throws IOException, ClassNotFoundException {

        Timer test = new Timer();
        test.setSchedule(context -> null);

        Timer copy = OddjobTestHelper.copy(test);

        assertNull(copy.getSchedule());
    }

    @Test
    public void testPersistedScheduleInOddjob() throws FailedToStopException, ArooaPropertyException, ArooaConversionException, InterruptedException, IOException {

        File persistDir = OurDirs.workPathDir(
                getClass().getSimpleName() + "/persisted-schedule", false)
                .toFile();
        if (persistDir.exists()) {
            FileUtils.forceDelete(persistDir);
        }

        Oddjob oddjob1 = new Oddjob();
        oddjob1.setFile(OurDirs.relativePath("test/conf/persisted-schedule.xml")
                .toFile());

        oddjob1.setExport("clock", new ArooaObject(
                new ManualClock("2011-03-09 06:30")));
        oddjob1.setExport("work-dir", new ArooaObject(
                persistDir.getParent()));

        oddjob1.run();

        assertEquals(ParentState.STARTED,
                oddjob1.lastStateEvent().getState());

        assertEquals(new SimpleInterval(
                        DateHelper.parseDateTime("2011-03-10 05:30"),
                        DateHelper.parseDateTime("2011-03-10 06:30")),
                new OddjobLookup(oddjob1).lookup("persisted-schedule/schedule1.current"));
        assertEquals(DateHelper.parseDateTime("2011-03-10 05:30"),
                new OddjobLookup(oddjob1).lookup("persisted-schedule/schedule1.nextDue"));

        oddjob1.stop();

        assertEquals(ParentState.READY, oddjob1.lastStateEvent().getState());

        oddjob1.destroy();

        //
        // Second run

        Oddjob oddjob2 = new Oddjob();
        oddjob2.setFile(OurDirs.relativePath("test/conf/persisted-schedule.xml").toFile());

        oddjob2.setExport("clock", new ArooaObject(
                new ManualClock("2011-03-10 07:00")));
        oddjob2.setExport("work-dir", new ArooaObject(
                persistDir.getParent()));

        oddjob2.load();

        Oddjob innerOddjob2 = new OddjobLookup(oddjob2).lookup("persisted-schedule",
                Oddjob.class);

        innerOddjob2.load();

        Stateful scheduledJob2 = new OddjobLookup(innerOddjob2).lookup("scheduled-job",
                Stateful.class);

        StateSteps scheduledJobState2 = new StateSteps(scheduledJob2);

        scheduledJobState2.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        oddjob2.run();

        scheduledJobState2.checkWait();

        String text2 = new OddjobLookup(oddjob2).lookup(
                "persisted-schedule/scheduled-job.text", String.class);

        assertEquals("Job schedule at 2011-03-10 05:30:00.000 " +
                        "but running at 2011-03-10 07:00:00.000",
                text2);

        oddjob2.stop();

        assertEquals(ParentState.READY, oddjob2.lastStateEvent().getState());

        oddjob2.destroy();

        //
        // Third run

        Oddjob oddjob3 = new Oddjob();
        oddjob3.setFile(OurDirs.relativePath("test/conf/persisted-schedule.xml")
                .toFile());

        oddjob3.setExport("clock", new ArooaObject(
                new ManualClock("2011-03-10 08:00")));
        oddjob3.setExport("work-dir", new ArooaObject(
                persistDir.getParent()));

        oddjob3.run();

        assertEquals(new SimpleInterval(
                        DateHelper.parseDateTime("2011-03-11 05:30"),
                        DateHelper.parseDateTime("2011-03-11 06:30")),
                new OddjobLookup(oddjob3).lookup("persisted-schedule/schedule1.current"));
        assertEquals(DateHelper.parseDateTime("2011-03-11 05:30"),
                new OddjobLookup(oddjob3).lookup("persisted-schedule/schedule1.nextDue"));

        String text3 = new OddjobLookup(oddjob3).lookup(
                "persisted-schedule/scheduled-job.text", String.class);

        assertEquals("Job schedule at 2011-03-10 05:30:00.000 " +
                        "but running at 2011-03-10 07:00:00.000",
                text3);

        oddjob3.stop();

        assertEquals(ParentState.READY, oddjob3.lastStateEvent().getState());

        oddjob3.destroy();
    }

    @Test
    public void testTimerExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/TimerExample.xml",
                getClass().getClassLoader()));

        oddjob.load();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Timer timer = lookup.lookup("timer", Timer.class);

        ManualClock clock = new ManualClock("2011-04-08 09:59:59.750");
        timer.setClock(clock);

        Stateful work = lookup.lookup("work", Stateful.class);

        StateSteps workState = new StateSteps(work);
        workState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        oddjob.run();

        workState.checkWait();

        assertEquals(DateHelper.parseDateTime("2011-04-11 10:00"),
                timer.getNextDue());

        String result = lookup.lookup("work.text", String.class);

        assertEquals("Doing some work at 2011-04-08 10:00:00.000",
                result);

        oddjob.stop();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        //
        // Run again.

        oddjob.run();

        assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, work.lastStateEvent().getState());

        assertEquals(DateHelper.parseDateTime("2011-04-11 10:00"),
                timer.getNextDue());

        oddjob.destroy();
    }

    @Test
    public void testTimerOnceExample() throws ArooaPropertyException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/TimerOnceExample.xml",
                getClass().getClassLoader()));

        oddjob.load();

        assertEquals(ParentState.READY,
                oddjob.lastStateEvent().getState());

        Timer timer = (Timer) new OddjobLookup(oddjob).lookup("timer");

        // on slow systems might go straight to ACTIVE.
        timer.setClock(new ManualClock("2011-09-28 09:59:59.900"));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.STARTED,
                ParentState.ACTIVE,
                ParentState.COMPLETE);

        oddjob.run();

        states.checkWait();

        oddjob.destroy();
    }

    private static class OurOddjobExecutor extends MockOddjobExecutors {

        OurScheduledExecutorService executor =
                new OurScheduledExecutorService();

        @Override
        public ScheduledExecutorService getScheduledExecutor() {
            return executor;
        }

        @Override
        public ExecutorService getPoolExecutor() {
            return null;
        }

    }

    @Test
    public void testTimerStopJobExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        OurOddjobExecutor executors = new OurOddjobExecutor();

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/TimerStopJobExample.xml",
                getClass().getClassLoader()));
        oddjob.setOddjobExecutors(executors);

        oddjob.load();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful wait = lookup.lookup("long-job", Stateful.class);

        Stateful timer = lookup.lookup("timer", Stateful.class);

        StateSteps waitStates = new StateSteps(wait);

        waitStates.startCheck(
                JobState.READY,
                JobState.EXECUTING);

        StateSteps timerStates = new StateSteps(timer);

        timerStates.startCheck(TimerState.STARTABLE,
                TimerState.STARTING, TimerState.STARTED,
                TimerState.ACTIVE, TimerState.COMPLETE);

        Thread t = new Thread(oddjob);
        t.start();

        waitStates.checkWait();

        assertTrue(executors.executor.delay > 9000);

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(
                ParentState.EXECUTING,
                ParentState.STARTED,
                ParentState.ACTIVE,
                ParentState.COMPLETE);

        executors.executor.runnable.run();

        timerStates.checkWait();

        // We can't guarantee order because timer might complete before
        // sequential starts reflecting child states.
//    	states.checkWait();

        t.join();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }

    @Test
    public void testTimerCrashPersistance() {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <scheduling:timer id='timer' xmlns:scheduling='http://rgordon.co.uk/oddjob/scheduling'>" +
                        "   <schedule>" +
                        "    <schedules:daily at='07:00' xmlns:schedules='http://rgordon.co.uk/oddjob/schedules'/>" +
                        "   </schedule>" +
                        "   <clock>" +
                        "    <value value='${clock}'/>" +
                        "   </clock>" +
                        "   <job>" +
                        "    <echo>Hi</echo>" +
                        "   </job>" +
                        "  </scheduling:timer>" +
                        " </job>" +
                        "</oddjob>";

        OurOddjobExecutor executors1 = new OurOddjobExecutor();

        MapPersister persister = new MapPersister();

        Oddjob oddjob1 = new Oddjob();
        oddjob1.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob1.setExport("clock", new ArooaObject(
                new ManualClock("2011-12-23 07:00")));
        oddjob1.setOddjobExecutors(executors1);
        oddjob1.setPersister(persister);
        oddjob1.run();

        assertEquals(ParentState.ACTIVE, oddjob1.lastStateEvent().getState());

        assertEquals(0, executors1.executor.delay);

        executors1.executor.runnable.run();

        assertEquals(24 * 60 * 60 * 1000L, executors1.executor.delay);

        OurOddjobExecutor executors2 = new OurOddjobExecutor();

        Oddjob oddjob2 = new Oddjob();
        oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob2.setExport("clock", new ArooaObject(
                new ManualClock("2011-12-23 07:00")));
        oddjob2.setOddjobExecutors(executors2);
        oddjob2.setPersister(persister);
        oddjob2.run();

        assertEquals(24 * 60 * 60 * 1000L, executors2.executor.delay);
    }

    private static class OurFuture extends MockScheduledFuture<Void> {
        boolean cancelled;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            this.cancelled = true;
            return true;
        }
    }

    @Test
    public void testSetNextDue() throws ArooaPropertyException, ArooaConversionException, FailedToStopException, ParseException {

        ManualClock clock = new ManualClock("2012-12-27 08:00");

        OurFuture future = new OurFuture();
        OurOddjobExecutor executors = new OurOddjobExecutor();
        executors.executor.future = future;

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/TimerSetNextDueExample.xml",
                getClass().getClassLoader()));
        oddjob.setOddjobExecutors(executors);
        oddjob.setExport("clock", new ArooaObject(clock));

        oddjob.run();

        assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Timer test = lookup.lookup("timer", Timer.class);

        assertEquals(DateHelper.parseDate("9999-12-31"), test.getNextDue());

        assertNotNull(executors.executor.runnable);
        assertEquals(252045619200000L, executors.executor.delay);
        assertEquals(false, future.cancelled);


        Runnable set = lookup.lookup("set", Runnable.class);

        set.run();

        assertEquals(DateHelper.parseDateTime("2012-12-27 08:02"),
                test.getNextDue());

        assertEquals(2 * 60 * 1000, executors.executor.delay);
        assertEquals(true, future.cancelled);

        executors.executor.runnable.run();

        assertEquals("Running at 9999-12-31 00:00:00.000", lookup.lookup("echo.text"));

        oddjob.stop();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());


        oddjob.destroy();
    }

    @Test
    public void testSetReshedule() throws ArooaPropertyException, ArooaConversionException, FailedToStopException {

        ManualClock clock = new ManualClock("2013-01-16 08:00");

        OurFuture future = new OurFuture();
        OurOddjobExecutor executors = new OurOddjobExecutor();
        executors.executor.future = future;

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/scheduling/TimerSetRescheduleExample.xml",
                getClass().getClassLoader()));
        oddjob.setOddjobExecutors(executors);
        oddjob.setExport("clock", new ArooaObject(clock));

        oddjob.run();

        assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Timer test = lookup.lookup("timer", Timer.class);

        assertEquals(DateHelper.parseDateTime("2013-01-16 23:00"),
                test.getNextDue());

        assertNotNull(executors.executor.runnable);
        assertEquals(15 * 60 * 60 * 1000L, executors.executor.delay);
        assertEquals(false, future.cancelled);

        Runnable set = lookup.lookup("set", Runnable.class);

        set.run();

        assertEquals(DateHelper.parseDateTime("2013-01-17 23:00"),
                test.getNextDue());

        assertEquals(39 * 60 * 60 * 1000L, executors.executor.delay);
        assertEquals(true, future.cancelled);

        executors.executor.runnable.run();

        assertEquals("Running at 2013-01-17 23:00:00.000",
                lookup.lookup("echo.text"));

        oddjob.stop();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        oddjob.destroy();
    }
}
