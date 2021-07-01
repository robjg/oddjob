package org.oddjob.beanbus.mega;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oddjob.Stateful;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.SimpleBusConductor;
import org.oddjob.beanbus.destinations.Batcher;
import org.oddjob.beanbus.destinations.BeanCapture;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

public class StatefulBusSupervisorTest {


    @Test
    public void testStartedAndStopped() throws BusCrashException {

        BusControls busControls = mock(BusControls.class);

        FlagState flag = new FlagState();

        StatefulBusSupervisor.BusAction test =
                new StatefulBusSupervisor(busControls, Runnable::run)
                    .supervise(flag);

        flag.run();

        test.run();

        verify(busControls, times(1)).stopBus();

        // test listener removed ok.

        flag.hardReset();
        flag.run();

        verifyNoMoreInteractions(busControls);
    }

    @Test
    public void testStartedAndCrashed() throws BusCrashException {

        BusControls busControls = mock(BusControls.class);

        FlagState flag = new FlagState(JobState.EXCEPTION);

        StatefulBusSupervisor test =
                new StatefulBusSupervisor(busControls, Runnable::run);

        StatefulBusSupervisor.BusAction supervisorAction;
        supervisorAction = test.supervise(flag);

        flag.run();

        supervisorAction.run();

        verify(busControls, times(1)).crashBus(any(Exception.class));
        verifyNoMoreInteractions(busControls);
        reset(busControls);

        flag.setState(JobState.COMPLETE);

        flag.hardReset();

        supervisorAction = test.supervise(flag);

        flag.run();

        supervisorAction.run();

        verify(busControls, times(1)).stopBus();
        verifyNoMoreInteractions(busControls);
    }

    @Test
    public void testWhenListenersAddedAndRemovedWhenStartingAndStopping() throws BusCrashException, InterruptedException {

        FlagState flag = new FlagState();

        CountDownLatch latch = new CountDownLatch(1);

        Stateful job1 = mock(Stateful.class);
        Stateful job2 = mock(Stateful.class);
        doAnswer(invocation -> {latch.countDown();
            return null; })
                .when(job2).addStateListener(any(StateListener.class));
        BusControls busControls = mock(BusControls.class);

        StatefulBusSupervisor.BusAction test =
                new StatefulBusSupervisor(busControls, Runnable::run)
                        .supervise(job1, job2);

        ArgumentCaptor<StateListener> l1ac = ArgumentCaptor.forClass(StateListener.class);
        ArgumentCaptor<StateListener> l2ac = ArgumentCaptor.forClass(StateListener.class);

        verify(job1, times(1)).addStateListener(l1ac.capture());
        verify(job2, times(1)).addStateListener(l2ac.capture());


        AtomicReference<BusCrashException> er = new AtomicReference<>();

        Thread t = new Thread(() -> {
            try {
                test.run();
            } catch (BusCrashException e) {
                er.set(e);
            }
        });
        t.start();

        latch.await();

        verifyNoMoreInteractions(busControls, job1, job2);

        l1ac.getValue().jobStateChange(new StateEvent(job1, JobState.COMPLETE));

        t.join();

        verify(busControls, times(1)).stopBus();

        verify(job1, times(1)).removeStateListener(eq(l1ac.getValue()));
        verify(job2, times(1)).removeStateListener(eq(l2ac.getValue()));

        verifyNoMoreInteractions(busControls, job1, job2);

        assertThat(er.get(), nullValue());
    }

    private static class OurJob extends SimpleJob {

        private Consumer<String> to;

        @Override
        protected int execute() throws Throwable {
            to.accept("apples");
            to.accept("oranges");
            to.accept("pears");
            return 0;
        }
    }

    @Test
    public void testCleanBusWithBatcher() throws BusCrashException {

        Batcher<String> batcher = new Batcher<>();
        batcher.setBatchSize(2);

        BeanCapture<Collection<String>> results =
                new BeanCapture<>();

        results.run();

        OurJob job = new OurJob();

        job.to = batcher;
        batcher.setTo(results);

        SimpleBusConductor busConductor = new SimpleBusConductor(job, batcher, results);

        BusControls busControls = mock(BusControls.class);

        StatefulBusSupervisor test =
                new StatefulBusSupervisor(busControls, Runnable::run);

        StatefulBusSupervisor.BusAction supervisorAction;
        supervisorAction = test.supervise(job, batcher, results);

        busConductor.run();

        assertThat(job.lastStateEvent().getState(), is(JobState.COMPLETE));

        supervisorAction.run();

        verify(busControls, times(1)).stopBus();
        verifyNoMoreInteractions(busControls);

        busConductor.close();

        assertThat(batcher.getCount(), is(3));
        assertThat(results.getCount(), is(2));

        reset(busControls);

        job.hardReset();
        batcher.reset();

        supervisorAction = test.supervise(job, batcher, results);

        busConductor.run();

        assertThat(job.lastStateEvent().getState(), is(JobState.COMPLETE));

        supervisorAction.run();

        verify(busControls, times(1)).stopBus();
        verifyNoMoreInteractions(busControls);

        busConductor.close();

        assertThat(batcher.getCount(), is(3));
        assertThat(results.getCount(), is(2));
    }

    private static class NaughtyDestination implements Consumer<String> {

        @Override
        public void accept(String e) {
            throw new RuntimeException("Naughty!");
        }
    }

    @Test
    public void testWithNaughtyDestination() throws BusCrashException {

        NaughtyDestination naughty = new NaughtyDestination();

        OurJob job = new OurJob();

        job.to = naughty;

        SimpleBusConductor busConductor = new SimpleBusConductor(job, naughty);

        BusControls busControls = mock(BusControls.class);

        StatefulBusSupervisor.BusAction test =
                new StatefulBusSupervisor(busControls, Runnable::run)
                        .supervise(job, naughty);

        busConductor.run();

        test.run();

        assertThat(job.lastStateEvent().getState(), is(JobState.EXCEPTION));

        ArgumentCaptor<Throwable> throwableArgumentCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(busControls, times(1)).crashBus(throwableArgumentCaptor.capture());
        verifyNoMoreInteractions(busControls);

        busConductor.actOnBusCrash(throwableArgumentCaptor.getValue());
    }
}
