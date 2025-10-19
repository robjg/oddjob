/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.Service;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.framework.util.StopWait;
import org.oddjob.jobs.WaitJob;
import org.oddjob.jobs.structural.JobFolder;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.oddjob.state.JoinJobTest.TIMEOUT;

/**
 *
 */
public class CascadeJobTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(CascadeJobTest.class);

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
    public void testEmpty() throws InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.READY);

        test.run();

        steps.checkWait();

        executors.stop();
    }

    @Test
    public void testSimpleRunnables() throws InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        OurJob job1 = new OurJob();
        OurJob job2 = new OurJob();
        OurJob job3 = new OurJob();

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(1, job2);
        test.setJobs(2, job3);

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE, ParentState.COMPLETE);

        test.run();

        testState.checkWait();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());

        assertEquals(1, job1.ran);
        assertEquals(1, job2.ran);
        assertEquals(1, job3.ran);

        ((Resettable) job2).hardReset();

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.READY, job2.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());

        test.hardReset();

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        assertEquals(JobState.READY, job1.lastStateEvent().getState());
        assertEquals(JobState.READY, job2.lastStateEvent().getState());
        assertEquals(JobState.READY, job3.lastStateEvent().getState());

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        test.run();

        testState.checkWait();

        assertEquals(2, job1.ran);
        assertEquals(2, job2.ran);
        assertEquals(2, job3.ran);

        executors.stop();
    }


    @Test
    public void testNotComplete() throws InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState();
        job1.setState(JobState.INCOMPLETE);

        FlagState job2 = new FlagState();
        job2.setState(JobState.INCOMPLETE);

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(1, job2);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        StateSteps testStates = new StateSteps(test);
        testStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.INCOMPLETE);

        test.run();

        testStates.checkWait();

        assertEquals(JobState.INCOMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.READY, job2.lastStateEvent().getState());

        job1.setState(JobState.COMPLETE);

        testStates.startCheck(ParentState.INCOMPLETE, ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.INCOMPLETE);

        test.softReset();
        test.run();

        testStates.checkWait();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.INCOMPLETE, job2.lastStateEvent().getState());

        executors.stop();
    }

    @Test
    public void testException() throws FailedToStopException, InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState();
        job1.setState(JobState.COMPLETE);

        FlagState job2 = new FlagState();
        job2.setState(JobState.EXCEPTION);

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(1, job2);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        StateSteps job2Check = new StateSteps(job2);

        job2Check.startCheck(JobState.READY,
                JobState.EXECUTING, JobState.EXCEPTION);

        test.run();

        job2Check.checkWait();

        new StopWait(test).run();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.EXCEPTION, job2.lastStateEvent().getState());
        assertEquals(ParentState.EXCEPTION, test.lastStateEvent().getState());

        job2Check.startCheck(JobState.EXCEPTION, JobState.READY,
                JobState.EXECUTING, JobState.COMPLETE);

        job2.setState(JobState.COMPLETE);
        job2.softReset();
        job2.run();

        job2Check.checkWait();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());

        executors.stop();
    }

    private static class OurService implements Service {

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }

    @Test
    public void testServiceAndException() throws FailedToStopException, InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        Stateful service1 = (Stateful) new OddjobComponentResolver().resolve(
                new OurService(), new StandardArooaSession());

        FlagState job2 = new FlagState();
        job2.setState(JobState.EXCEPTION);

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());
        test.setCascadeOn(StateConditions.ENDED);
        test.setJobs(0, service1);
        test.setJobs(1, job2);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        StateSteps service1States = new StateSteps(service1);
        service1States.startCheck(ServiceState.STARTABLE,
                ServiceState.STARTING, ServiceState.STARTED);

        StateSteps job2States = new StateSteps(job2);
        job2States.startCheck(JobState.READY,
                JobState.EXECUTING, JobState.EXCEPTION);

        test.run();

        service1States.checkWait();

        assertEquals(JobState.READY, job2.lastStateEvent().getState());

        ((Stoppable) service1).stop();

        job2States.checkWait();

        new StopWait(test).run();

        assertEquals(ServiceState.STOPPED, service1.lastStateEvent().getState());
        assertEquals(JobState.EXCEPTION, job2.lastStateEvent().getState());
        assertEquals(ParentState.EXCEPTION, test.lastStateEvent().getState());

        job2States.startCheck(JobState.EXCEPTION, JobState.READY,
                JobState.EXECUTING, JobState.COMPLETE);

        job2.setState(JobState.COMPLETE);
        job2.softReset();
        job2.run();

        job2States.checkWait();

        assertEquals(ServiceState.STOPPED, service1.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());

        executors.stop();
    }

    @Test
    public void testDestroyed() throws InterruptedException {
        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState();
        job1.setState(JobState.COMPLETE);

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        test.setJobs(0, job1);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());

        StateSteps state = new StateSteps(test);
        state.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        test.run();

        state.checkWait();

        final List<State> results = new ArrayList<>();

        class OurListener implements StateListener {

            public void jobStateChange(StateEvent event) {
                results.add(event.getState());
            }
        }
        OurListener l = new OurListener();
        test.addStateListener(l);

        assertEquals(ParentState.COMPLETE, results.get(0));
        assertEquals(1, results.size());

        test.destroy();

        assertEquals(ParentState.DESTROYED, results.get(1));
        assertEquals(2, results.size());

        executors.stop();
    }

    @Test
    public void testWithFoldersMixedIn() throws InterruptedException {

        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState(JobState.COMPLETE);
        FlagState job2 = new FlagState(JobState.COMPLETE);

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        test.setJobs(0, new JobFolder());
        test.setJobs(1, job1);
        test.setJobs(2, new JobFolder());
        test.setJobs(3, job2);
        test.setJobs(4, new JobFolder());

        test.run();

        testState.checkWait();

        executors.stop();
    }

    @Test
    public void testRemovingAndInserting() throws InterruptedException {

        DefaultExecutors executors = new DefaultExecutors();

        FlagState job1 = new FlagState(JobState.COMPLETE);
        FlagState job2 = new FlagState(JobState.COMPLETE);

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        test.setJobs(0, job1);
        test.setJobs(1, job2);

        test.run();

        testState.checkWait();

        test.setJobs(0, null);

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

        testState.startCheck(ParentState.COMPLETE, ParentState.EXECUTING,
                ParentState.COMPLETE);

        FlagState job3 = new FlagState(JobState.COMPLETE);

        StateSteps job3State = new StateSteps(job3);

        job3State.startCheck(JobState.READY);

        test.setJobs(1, job3);

        job3State.checkNow();

        FlagState job4 = new FlagState(JobState.COMPLETE);

        test.setJobs(0, job4);

        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        assertEquals(JobState.READY, job4.lastStateEvent().getState());

        executors.stop();
    }

    @Test
    public void testInsertingWhileRunning() throws InterruptedException, FailedToStopException {

        DefaultExecutors executors = new DefaultExecutors();

        WaitJob job1 = new WaitJob();
        job1.setName("Wait-1");
        WaitJob job2 = new WaitJob();
        job2.setName("Wait-2");
        WaitJob job3 = new WaitJob();
        job3.setName("Wait-3");
        WaitJob job4 = new WaitJob();
        job4.setName("Wait-4");

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(1, job2);

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        StateSteps job1State = new StateSteps(job1);
        job1State.startCheck(JobState.READY, JobState.EXECUTING);

        StateSteps job2State = new StateSteps(job2);
        job2State.startCheck(JobState.READY);

        test.run();

        StateSteps job3State = new StateSteps(job3);
        job3State.startCheck(JobState.READY, JobState.EXECUTING);

        test.setJobs(1, null);
        test.setJobs(1, job3);

        job1State.checkWait();
        job1State.startCheck(JobState.EXECUTING, JobState.COMPLETE);

        job1.stop();

        job1State.checkWait();
        job2State.checkNow();
        job3State.checkWait();

        StateSteps job4State = new StateSteps(job4);
        job4State.startCheck(JobState.READY, JobState.EXECUTING);

        test.setJobs(0, job4);

        new Thread(job4).start();

        job4State.checkWait();

        job3State.startCheck(JobState.EXECUTING, JobState.COMPLETE);
        job4State.startCheck(JobState.EXECUTING, JobState.COMPLETE);

        job3.stop();

        job3State.checkWait();

        test.stop();

        job4State.checkNow();

        testState.checkNow();

        executors.stop();
    }

    @Test
    public void testInOddjob() throws InterruptedException {

        String xml =
                "<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
                        " <job>" +
                        "  <state:cascade>" +
                        "   <jobs>" +
                        "    <echo>one</echo>" +
                        "    <echo>two</echo>" +
                        "    <echo>three</echo>" +
                        "   </jobs>" +
                        "  </state:cascade>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        oddjob.run();

        oddjobStates.checkWait();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
    }

    @Test
    public void testGivenCascadeThenStopStopsChildTasks() throws InterruptedException, FailedToStopException {

        DefaultExecutors executors = new DefaultExecutors();

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors.getPoolExecutor());

        WaitJob wait1 = new WaitJob();
        wait1.setName("Wait1");
        WaitJob wait2 = new WaitJob();
        wait2.setName("Wait2");

        test.setJobs(0, wait1);
        test.setJobs(1, wait2);

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        StateSteps wait1State = new StateSteps(wait1);
        StateSteps wait2State = new StateSteps(wait2);

        wait1State.startCheck(JobState.READY, JobState.EXECUTING);

        logger.info("** Running cascade.");

        test.run();

        wait1State.checkWait();

        testState.checkNow();

        assertThat(wait2.lastStateEvent().getState(), is(JobState.READY));

        testState.startCheck(ParentState.ACTIVE, ParentState.READY);

        logger.info("** Stopping cascade");

        test.stop();

        testState.checkWait();

        assertThat(wait1.lastStateEvent().getState(), is(JobState.COMPLETE));
        assertThat(wait2.lastStateEvent().getState(), is(JobState.READY));

        //
        // Second run.

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        wait2State.startCheck(JobState.READY, JobState.EXECUTING);

        logger.info("** Running cascade again");

        // Fast computer will see events as being the same.
        Thread.sleep(2L);

        test.run();

        wait2State.checkWait();

        testState.checkWait();


        logger.info("** Stopping cascade again");

        testState.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

        test.stop();

        testState.checkWait();

        assertThat(wait1.lastStateEvent().getState(), is(JobState.COMPLETE));
        assertThat(wait2.lastStateEvent().getState(), is(JobState.COMPLETE));

        executors.stop();
    }

    @Test
    public void testCascadeWhenExecutionOrderDifferent() throws FailedToStopException {

        List<Runnable> jobs = new ArrayList<>();

        ExecutorService executors = Mockito.mock(ExecutorService.class);
        Mockito.doAnswer(invocation -> {
            Runnable job = invocation.getArgument(0, Runnable.class);
            jobs.add(job);
            return null;
        }).when(executors).submit(Mockito.any(Runnable.class));

        CascadeJob test = new CascadeJob();
        test.setExecutorService(executors);

        FlagState job1 = new FlagState();
        job1.setName("Job1");
        FlagState job2 = new FlagState();
        job2.setName("Job2");

        test.setJobs(0, job1);
        test.setJobs(1, job2);

        StateSteps testState = new StateSteps(test);

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        StateSteps job1State = new StateSteps(job1);
        StateSteps job2State = new StateSteps(job2);

        job1State.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        logger.info("** Running cascade.");

        test.run();

        assertThat(jobs.size(), is(1));

        jobs.get(0).run();

        assertThat(jobs.size(), is(2));


        job1State.checkNow();
        testState.checkNow();

        assertThat(job2.lastStateEvent().getState(), is(JobState.READY));

        testState.startCheck(ParentState.ACTIVE, ParentState.READY);

        logger.info("** Stopping cascade");

        test.stop();

        testState.checkNow();

        assertThat(job1.lastStateEvent().getState(), is(JobState.COMPLETE));
        assertThat(job2.lastStateEvent().getState(), is(JobState.READY));

        //
        // Second run.

        jobs.clear();

        testState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        job2State.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        logger.info("** Running cascade again");

        test.run();

        assertThat(jobs.size(), is(2));

        jobs.get(0).run();

        assertThat(jobs.size(), is(2));

        jobs.get(1).run();

        job2State.checkNow();

        testState.checkNow();

        logger.info("** Stopping cascade again");

        test.stop();

        assertThat(job1.lastStateEvent().getState(), is(JobState.COMPLETE));
        assertThat(job2.lastStateEvent().getState(), is(JobState.COMPLETE));
    }

    @Test
    public void testExample() throws InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/state/CascadeExample.xml",
                getClass().getClassLoader()));

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = console.captureConsole()) {

            oddjob.run();

            oddjobStates.checkWait();

        }

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals(2, lines.length);
        assertEquals("This runs first.", lines[0].trim());
        assertEquals("Then this.", lines[1].trim());

        oddjob.destroy();
    }

    @Test
    public void testWithParallel() throws InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/state/CascadeWithParallelExample.xml",
                getClass().getClassLoader()));

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.COMPLETE);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = console.captureConsole()) {

            oddjob.run();

            oddjobStates.checkWait();
        }

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals(3, lines.length);
        assertEquals("Apples are guaranteed to be third.", lines[2].trim());

        oddjob.destroy();
    }

    @Test
    public void testCascadeOnHaltOnExample() throws InterruptedException {

        File file = new File(Objects.requireNonNull(getClass().getResource(
                "CascadeOnHaltOnExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.EXCEPTION);

        oddjob.run();

        oddjobStates.checkWait();

        Object[] jobs = OddjobTestHelper.getChildren(
                OddjobTestHelper.getChildren(oddjob)[0]);

        assertEquals(JobState.INCOMPLETE, ((Stateful)
                jobs[0]).lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, ((Stateful)
                jobs[1]).lastStateEvent().getState());
        assertEquals(JobState.EXCEPTION, ((Stateful)
                jobs[2]).lastStateEvent().getState());
        assertEquals(JobState.READY, ((Stateful)
                jobs[3]).lastStateEvent().getState());

        oddjob.destroy();
    }

    @Test
    public void testCascadeJoinExample() throws InterruptedException, ArooaPropertyException, ArooaConversionException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/state/CascadeJoinExample.xml",
                getClass().getClassLoader()));

        oddjob.load();

        assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        StateSteps oddjobState = new StateSteps(oddjob);

        Stateful parallel = lookup.lookup("parallel", Stateful.class);
        Stateful lastJob = lookup.lookup("last-job", Stateful.class);

        StateSteps parallelState = new StateSteps(parallel);
        StateSteps lastState = new StateSteps(lastJob);

        Thread t = new Thread(oddjob);

        parallelState.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE, ParentState.STARTED);
        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        logger.info("** starting first run");

        t.start();

        parallelState.checkWait();

        t.join(TIMEOUT);

        oddjobState.checkWait();

        assertEquals(JobState.READY,
                lastJob.lastStateEvent().getState());

        Object applesFlag = lookup.lookup("apples");
        Object orangesFlag = lookup.lookup("oranges");

        oddjobState.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);
        lastState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        ((Runnable) applesFlag).run();
        ((Runnable) orangesFlag).run();

        logger.info("** first run done.");

        lastState.checkWait();
        oddjobState.checkWait();

        logger.info("** resetting");

        ((Resettable) parallel).hardReset();
        ((Resettable) lastJob).hardReset();
        ((Resettable) applesFlag).hardReset();
        ((Resettable) orangesFlag).hardReset();

        Thread t2 = new Thread(oddjob);

        parallelState.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE, ParentState.STARTED);

        logger.info("** starting second run");

        t2.start();

        parallelState.checkWait();

        assertEquals(JobState.READY,
                lastJob.lastStateEvent().getState());

        lastState.startCheck(JobState.READY, JobState.EXECUTING, JobState.COMPLETE);

        ((Runnable) applesFlag).run();
        ((Runnable) orangesFlag).run();

        t2.join(TIMEOUT);

        logger.info("** second run done.");

        lastState.checkWait();

        oddjob.stop();
        oddjob.destroy();
    }
}
