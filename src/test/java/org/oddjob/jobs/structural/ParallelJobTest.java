package org.oddjob.jobs.structural;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.Service;
import org.oddjob.framework.util.StopWait;
import org.oddjob.jobs.WaitJob;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.state.*;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ParallelJobTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(ParallelJobTest.class);

    @Before
    public void setUp() throws Exception {


        logger.info("--------------------  " + getName() + "  ----------------");
    }

    @Test
    public void testThreeJobsWithDefaultExecutors() throws InterruptedException {

        FlagState job1 = new FlagState(JobState.COMPLETE);
        FlagState job2 = new FlagState(JobState.COMPLETE);
        FlagState job3 = new FlagState(JobState.COMPLETE);

        DefaultExecutors defaultServices = new DefaultExecutors();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.setExecutorService(defaultServices.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(1, job2);
        test.setJobs(2, job3);

        test.run();

        steps.checkWait();

        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());

        steps.startCheck(ParentState.COMPLETE, ParentState.READY);

        test.hardReset();

        assertEquals(JobState.READY, job1.lastStateEvent().getState());
        assertEquals(JobState.READY, job2.lastStateEvent().getState());
        assertEquals(JobState.READY, job3.lastStateEvent().getState());

        steps.checkNow();

        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.run();

        steps.checkWait();

        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());

        defaultServices.stop();
    }

    @Test
    public void testOneObjectSetsStateComplete() throws InterruptedException {

        Object job1 = new Object();

        DefaultExecutors defaultServices = new DefaultExecutors();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        test.setExecutorService(defaultServices.getPoolExecutor());

        test.setJobs(0, job1);

        test.run();

        steps.checkWait();


        steps.startCheck(ParentState.COMPLETE, ParentState.READY);

        test.hardReset();

        steps.checkNow();

        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        test.run();

        steps.checkWait();

        defaultServices.stop();
    }

    @Test
    public void testTwoJobsAndAnObjectSetsStateComplete() throws InterruptedException {

        Object job1 = new Object();
        FlagState job2 = new FlagState(JobState.COMPLETE);
        FlagState job3 = new FlagState(JobState.COMPLETE);

        DefaultExecutors defaultServices = new DefaultExecutors();

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.setExecutorService(defaultServices.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(1, job2);
        test.setJobs(2, job3);

        test.run();

        steps.checkWait();

        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());

        steps.startCheck(ParentState.COMPLETE, ParentState.READY);

        test.hardReset();

        assertEquals(JobState.READY, job2.lastStateEvent().getState());
        assertEquals(JobState.READY, job3.lastStateEvent().getState());

        steps.checkNow();

        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.run();

        // Sometimes the state goes to READY not ACTIVE - don't know why.
        steps.checkWait();

        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());

        defaultServices.stop();
    }

    @Test
    public void testThrottledExecution() throws InterruptedException {

        FlagState job1 = new FlagState(JobState.COMPLETE);
        FlagState job2 = new FlagState(JobState.COMPLETE);
        FlagState job3 = new FlagState(JobState.COMPLETE);

        DefaultExecutors defaultServices = new DefaultExecutors();
        defaultServices.setPoolSize(1);

        ParallelJob test = new ParallelJob();

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.setExecutorService(defaultServices.getPoolExecutor());

        test.setJobs(0, job1);
        test.setJobs(0, job2);
        test.setJobs(0, job3);

        test.run();

        steps.checkWait();

        assertEquals(JobState.COMPLETE, job3.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job2.lastStateEvent().getState());
        assertEquals(JobState.COMPLETE, job1.lastStateEvent().getState());
    }

    @Test
    public void testStopWithDefaultExecutorsAndOneJob() throws InterruptedException, FailedToStopException {

        DefaultExecutors defaultServices = new DefaultExecutors();

        ParallelJob test = new ParallelJob();
        test.setExecutorService(defaultServices.getPoolExecutor());

        WaitJob job = new WaitJob();

        test.setJobs(0, job);

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE);

        StateSteps waitState = new StateSteps(job);
        waitState.startCheck(JobState.READY,
                JobState.EXECUTING);

        test.run();

        steps.checkWait();
        waitState.checkWait();

        steps.startCheck(
                ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.stop();

        // Check now should work except there is some event listener order issue.
        steps.checkWait();

        test.destroy();

        defaultServices.stop();
    }

    private static class DestroyJob extends MockStateful
            implements Runnable {

        JobStateHandler handler = new JobStateHandler(this);

        public void addStateListener(StateListener listener) {
            handler.addStateListener(listener);
        }

        public void removeStateListener(StateListener listener) {
            handler.removeStateListener(listener);
        }

        public void run() {
            handler.waitToWhen(new IsAnyState(), () -> {
                handler.setState(JobState.COMPLETE);
                handler.fireEvent();
            });
        }

        void destroy() {
            handler.waitToWhen(new IsAnyState(), () -> {
                handler.setState(JobState.DESTROYED);
                handler.fireEvent();
            });
        }
    }

    /**
     * Child state changes before null is set as the child. Parallel job
     * shouldn't get that state.
     */
    @Test
    public void testChildDestroyed() {

        ParallelJob test = new ParallelJob();

        ExecutorService executorService = mock(ExecutorService.class);
        doAnswer(invocationOnMock -> {
            ((Runnable) invocationOnMock.getArgument(0)).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        test.setExecutorService(executorService);

        DestroyJob destroy = new DestroyJob();

        test.setJobs(0, destroy);

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        test.run();

        verify(executorService, timeout(1)).execute(any(Runnable.class));

        steps.checkNow();

        steps.startCheck(
                ParentState.COMPLETE, ParentState.DESTROYED);

        test.destroy();

        test.setJobs(0, null);

        destroy.destroy();

        steps.checkNow();

        verifyNoMoreInteractions(executorService);
    }

    @Test
    public void testInOddjob() throws InterruptedException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/SimpleParallelExample.xml",
                getClass().getClassLoader()));

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();

            new StopWait(oddjob).run();
        }

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        console.dump(logger);

        Set<String> results = Arrays.stream(console.getLines())
                .map(String::trim)
                .collect(Collectors.toSet());

        assertThat(results, Matchers.containsInAnyOrder("This runs in parallel",
                "With this which could be displayed first!"));

        oddjob.destroy();
    }

    @Test
    public void testStopInOddjob() throws ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <parallel>" +
                        "   <jobs>" +
                        "    <wait id='wait1'/>" +
                        "    <wait id='wait2'/>" +
                        "   </jobs>" +
                        "  </parallel>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.load();

        StateSteps oddjobState = new StateSteps(oddjob);

        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        StateSteps wait1State = new StateSteps(new OddjobLookup(oddjob).lookup(
                "wait1", Stateful.class));
        wait1State.startCheck(JobState.READY, JobState.EXECUTING);
        StateSteps wait2State = new StateSteps(new OddjobLookup(oddjob).lookup(
                "wait2", Stateful.class));
        wait2State.startCheck(JobState.READY, JobState.EXECUTING);

        Thread t = new Thread(oddjob);
        t.start();

        wait1State.checkWait();
        wait2State.checkWait();

        oddjobState.checkWait();

        oddjobState.startCheck(ParentState.ACTIVE,
                ParentState.COMPLETE);

        oddjob.stop();

        oddjobState.checkNow();

        oddjob.destroy();
    }

    public static class MyService implements Service {

        public void start() {
        }

        public void stop() {
        }
    }


    @Test
    public void testParallelServices() throws FailedToStopException, InterruptedException {

        DefaultExecutors defaultServices = new DefaultExecutors();

        ParallelJob test = new ParallelJob();

        test.setExecutorService(defaultServices.getPoolExecutor());

        Object service1 = new OddjobComponentResolver().resolve(
                new MyService(), null);
        Object service2 = new OddjobComponentResolver().resolve(
                new MyService(), null);

        test.setJobs(0, service1);
        test.setJobs(1, service2);

        StateSteps parallelStates = new StateSteps(test);

        parallelStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.STARTED);

        StateSteps service1States = new StateSteps((Stateful) service1);
        service1States.startCheck(ServiceState.STARTABLE,
                ServiceState.STARTING, ServiceState.STARTED);

        StateSteps service2States = new StateSteps((Stateful) service2);
        service2States.startCheck(ServiceState.STARTABLE,
                ServiceState.STARTING, ServiceState.STARTED);

        test.run();

        parallelStates.checkWait();

        service1States.checkWait();
        service2States.checkWait();

        parallelStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);
        service1States.startCheck(ServiceState.STARTED, ServiceState.STOPPED);
        service2States.startCheck(ServiceState.STARTED, ServiceState.STOPPED);

        test.stop();

        service1States.checkNow();
        service2States.checkNow();
        parallelStates.checkNow();

        defaultServices.stop();
    }

    @Test
    public void testJoin() throws FailedToStopException, InterruptedException {

        DefaultExecutors defaultServices = new DefaultExecutors();

        ParallelJob test = new ParallelJob();
        test.setJoin(true);

        test.setExecutorService(defaultServices.getPoolExecutor());

        Object service1 = new OddjobComponentResolver().resolve(
                new MyService(), null);
        Object service2 = new OddjobComponentResolver().resolve(
                new MyService(), null);

        test.setJobs(0, service1);
        test.setJobs(1, service2);

        StateSteps parallelStates = new StateSteps(test);

        parallelStates.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.STARTED);

        StateSteps service1States = new StateSteps((Stateful) service1);
        service1States.startCheck(ServiceState.STARTABLE,
                ServiceState.STARTING, ServiceState.STARTED);

        StateSteps service2States = new StateSteps((Stateful) service2);
        service2States.startCheck(ServiceState.STARTABLE,
                ServiceState.STARTING, ServiceState.STARTED);

        test.run();

        parallelStates.checkWait();

        service1States.checkWait();
        service2States.checkWait();

        parallelStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);
        service1States.startCheck(ServiceState.STARTED, ServiceState.STOPPED);
        service2States.startCheck(ServiceState.STARTED, ServiceState.STOPPED);

        test.stop();


        service1States.checkNow();
        service2States.checkNow();
        parallelStates.checkNow();

        defaultServices.stop();
    }

    @Test
    public void testEmpty() throws InterruptedException {

        ParallelJob test = new ParallelJob();

        ExecutorService executorService = mock(ExecutorService.class);

        test.setExecutorService(executorService);

        StateSteps steps = new StateSteps(test);
        steps.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.READY);

        test.run();

        steps.checkWait();

        verifyNoInteractions(executorService);

        test.destroy();
    }

    public static class SlowToStartService {

        public void start() throws InterruptedException {
            Thread.sleep(100);
        }

        public void stop() {

        }
    }

    @Test
    public void testParallelServiceExample() throws InterruptedException, FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ParallelServicesExample.xml",
                getClass().getClassLoader()));

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.STARTED);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();

            oddjobStates.checkWait();
        }

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals(1, lines.length);

        assertEquals("The lights are on and the machine goes ping.",
                lines[0].trim());

        oddjobStates.startCheck(ParentState.STARTED, ParentState.COMPLETE);

        oddjob.stop();

        oddjobStates.checkNow();

        oddjob.destroy();
    }

    @Test
    public void testParallelServiceThatCompletesExample()
            throws InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jobs/structural/ParallelServicesExample2.xml",
                getClass().getClassLoader()));

        StateSteps oddjobStates = new StateSteps(oddjob);

        oddjobStates.startCheck(ParentState.READY,
                ParentState.EXECUTING, ParentState.ACTIVE,
                ParentState.COMPLETE);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();

            oddjobStates.checkWait();
        }

        console.dump(logger);

        String[] lines = console.getLines();

        assertEquals(1, lines.length);

        assertEquals("The lights are on and the machine goes ping.",
                lines[0].trim());

        oddjob.destroy();
    }
}
