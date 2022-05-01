package org.oddjob.beanbus.bus;

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
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class StatefulBusSupervisorTest {

    @Test
    public void testStartedAndStopped() throws BusCrashException {

        BusControls busControls = mock(BusControls.class);

        FlagState flag = new FlagState();

        new StatefulBusSupervisor(busControls, Runnable::run)
                    .supervise(flag);

        flag.run();

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

        test.supervise(flag);

        flag.run();

        verify(busControls, times(1)).crashBus(any(Exception.class));
        verifyNoMoreInteractions(busControls);
        reset(busControls);

        flag.setState(JobState.COMPLETE);

        flag.hardReset();

        test.supervise(flag);

        flag.run();

        verify(busControls, times(1)).stopBus();
        verifyNoMoreInteractions(busControls);
    }

    @Test
    public void testWhenListenersAddedAndRemovedWhenStartingAndStopping() throws BusCrashException {

        FlagState flag = new FlagState();

        Stateful job1 = mock(Stateful.class);
        Stateful job2 = mock(Stateful.class);

        BusControls busControls = mock(BusControls.class);

        new StatefulBusSupervisor(busControls, Runnable::run)
                        .supervise(job1, job2);

        ArgumentCaptor<StateListener> l1ac = ArgumentCaptor.forClass(StateListener.class);
        ArgumentCaptor<StateListener> l2ac = ArgumentCaptor.forClass(StateListener.class);

        verify(job1, times(1)).addStateListener(l1ac.capture());
        verify(job2, times(1)).addStateListener(l2ac.capture());

        verifyNoMoreInteractions(busControls, job1, job2);

        l1ac.getValue().jobStateChange(new StateEvent(job1, JobState.COMPLETE));

        verify(busControls, times(1)).stopBus();

        l2ac.getValue().jobStateChange(new StateEvent(job2, JobState.COMPLETE));

        verify(job1, times(1)).removeStateListener(eq(l1ac.getValue()));
        verify(job2, times(1)).removeStateListener(eq(l2ac.getValue()));

        verifyNoMoreInteractions(busControls, job1, job2);
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

        test.supervise(job, batcher, results);

        busConductor.run();

        assertThat(job.lastStateEvent().getState(), is(JobState.COMPLETE));

        verify(busControls, times(1)).stopBus();
        verifyNoMoreInteractions(busControls);

        busConductor.close();

        assertThat(batcher.getCount(), is(3));
        assertThat(results.getCount(), is(2));

        reset(busControls);

        job.hardReset();
        batcher.reset();

        test.supervise(job, batcher, results);

        busConductor.run();

        assertThat(job.lastStateEvent().getState(), is(JobState.COMPLETE));

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

        new StatefulBusSupervisor(busControls, Runnable::run)
                        .supervise(job, naughty);

        busConductor.run();

        assertThat(job.lastStateEvent().getState(), is(JobState.EXCEPTION));

        ArgumentCaptor<Throwable> throwableArgumentCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(busControls, times(1)).crashBus(throwableArgumentCaptor.capture());
        verifyNoMoreInteractions(busControls);

        busConductor.actOnBusCrash(throwableArgumentCaptor.getValue());
    }
}
