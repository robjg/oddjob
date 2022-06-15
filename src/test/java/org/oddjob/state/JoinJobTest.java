/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.Timer;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JoinJobTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(JoinJobTest.class);

    static final long TIMEOUT = 15_000L;

    @Before
    public void setUp() throws Exception {


        logger.info("----------------  " + getName() + "  -----------------");
    }

    private static class OurJob extends SimpleJob {

        int ran;

        @Override
        protected int execute() throws Throwable {
            ++ran;
            return 0;
        }
    }


    // an empty sequence must be ready. This is to agree with oddjob
    // which must also be ready when reset and empty.
    // this is really a bug in StatefulChildHelper. An empty sequence should
    // be ready until run and then be complete. I think.
    @Test
    public void testEmpty() {

        JoinJob test = new JoinJob();
        test.setTimeout(TIMEOUT);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        test.run();

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

    }

    @Test
    public void testSimpleRunnable() throws FailedToStopException, InterruptedException {

        OurJob job1 = new OurJob();

        JoinJob test = new JoinJob();
        test.setTimeout(TIMEOUT);

        test.setJob(job1);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.COMPLETE);

        test.run();

        testState.checkNow();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());

        assertEquals(1, job1.ran);

        ((Resettable) job1).hardReset();

        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        assertEquals(JobState.READY, job1.lastStateEvent().getState());

        testState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.COMPLETE);

        test.run();

        testState.checkNow();

        assertEquals(2, job1.ran);
    }


    @Test
    public void testNotComplete() throws FailedToStopException {

        FlagState job1 = new FlagState();
        job1.setState(JobState.INCOMPLETE);


        JoinJob test = new JoinJob();
        test.setTimeout(TIMEOUT);

        test.setJob(job1);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        test.run();

        assertEquals(ParentState.INCOMPLETE, test.lastStateEvent().getState());

        job1.setState(JobState.COMPLETE);
        job1.hardReset();

        job1.run();

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
    }

    @Test
    public void testAsynchronous() throws FailedToStopException, InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState();
        job1.setState(JobState.COMPLETE);

        Timer timer = new Timer();

        CountSchedule count = new CountSchedule(1);
        IntervalSchedule interval = new IntervalSchedule(500);
        count.setRefinement(interval);
        timer.setSchedule(count);
        timer.setJob(job1);
        timer.setScheduleExecutorService(executors.getScheduledExecutor());

        JoinJob test = new JoinJob();
        test.setTimeout(TIMEOUT);

        test.setJob(timer);

        StateSteps testStates = new StateSteps(test);

        testStates.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.COMPLETE);

        test.run();

        testStates.checkNow();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        executors.stop();
    }

    @Test
    public void testAsynchronousStop() throws FailedToStopException, InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState();
        job1.setState(JobState.COMPLETE);

        Timer timer = new Timer();

        CountSchedule count = new CountSchedule(1);
        IntervalSchedule interval = new IntervalSchedule(1000000L);
        count.setRefinement(interval);
        timer.setSchedule(count);
        timer.setJob(job1);
        timer.setScheduleExecutorService(executors.getScheduledExecutor());

        final JoinJob test = new JoinJob();
        test.setTimeout(TIMEOUT);
        test.setJob(timer);

        StateSteps testStates = new StateSteps(test);

        testStates.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.COMPLETE);

        executors.getScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    test.stop();
                } catch (FailedToStopException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500, TimeUnit.MILLISECONDS);


        test.run();

        testStates.checkNow();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        executors.stop();
    }

    @Test
    public void testDestroyed() throws FailedToStopException {

        FlagState job1 = new FlagState();
        job1.setState(JobState.COMPLETE);

        JoinJob test = new JoinJob();
        test.setTimeout(TIMEOUT);

        test.setJob(job1);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        test.run();

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        StateSteps testStates = new StateSteps(test);

        testStates.startCheck(ParentState.COMPLETE,
                ParentState.DESTROYED);

        test.destroy();

        testStates.checkNow();
    }

    @Test
    public void testInOddjob() throws InterruptedException, ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/state/JoinExample.xml",
                getClass().getClassLoader()));

        oddjob.load();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Stateful test = lookup.lookup("our-join", Stateful.class);

        StateSteps testState = new StateSteps(test);

        Thread t = new Thread(oddjob);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING);

        logger.info("** starting first run");

        t.start();

        testState.checkWait();

        assertEquals(ParentState.EXECUTING,
                oddjob.lastStateEvent().getState());

        Stateful lastJob = lookup.lookup("last-job", Stateful.class);

        assertEquals(JobState.READY,
                lastJob.lastStateEvent().getState());

        Object applesFlag = lookup.lookup("apples");
        Object orangesFlag = lookup.lookup("oranges");

        ((Runnable) applesFlag).run();
        ((Runnable) orangesFlag).run();

        t.join(TIMEOUT);

        logger.info("** first run done.");

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        assertEquals(JobState.COMPLETE,
                lastJob.lastStateEvent().getState());

        logger.info("** resetting");

        ((Resettable) test).hardReset();
        ((Resettable) lastJob).hardReset();
        ((Resettable) applesFlag).hardReset();
        ((Resettable) orangesFlag).hardReset();

        Thread t2 = new Thread(oddjob);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING);

        logger.info("** starting second run");

        t2.start();

        testState.checkWait();

        assertEquals(JobState.READY,
                lastJob.lastStateEvent().getState());

        ((Runnable) applesFlag).run();
        ((Runnable) orangesFlag).run();

        t2.join(TIMEOUT);

        logger.info("** second run done.");

        assertEquals(JobState.COMPLETE,
                lastJob.lastStateEvent().getState());
    }
}
