package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.Iconic;
import org.oddjob.OjTestCase;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.images.IconHelper;
import org.oddjob.state.JobState;
import org.oddjob.tools.IconSteps;
import org.oddjob.tools.OddjobTestHelper;

import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.hamcrest.Matchers.is;

public class JobWrapperStopTest extends OjTestCase {

    private static final class WaitingJob implements Runnable {

        CyclicBarrier barrier = new CyclicBarrier(2);

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e1) {
                throw new RuntimeException(e1);
            }
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    public void testStopViaInterrupt() throws InterruptedException, BrokenBarrierException, FailedToStopException {

        ArooaSession session = new StandardArooaSession();

        WaitingJob job = new WaitingJob();

        Optional<JobAdaptor> jobAdaptor = new JobStrategies().adapt(job, session);
        assertThat(jobAdaptor.isPresent(), is(true));

        Runnable proxy = (Runnable) new JobProxyGenerator().generate(
                jobAdaptor.get(),
                getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(new StandardArooaSession());

        IconSteps icons = new IconSteps((Iconic) proxy);
        icons.startCheck(IconHelper.READY, IconHelper.EXECUTING,
                IconHelper.STOPPING, IconHelper.COMPLETE);

        Thread t = new Thread(proxy);

        t.start();

        job.barrier.await();

        ((Stoppable) proxy).stop();

        t.join();

        icons.checkNow();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));
    }

    // Annotating works on the base class but can't be overridden so make it final.
    abstract public static class StopBase {

        abstract void nowStop();

        @Stop
        final public void wow() {
            nowStop();
        }
    }

    public static class AnnotatedWaitingJob extends StopBase implements Runnable {

        CountDownLatch barrier = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }

        @Override
        void nowStop() {
            barrier.countDown();
        }

    }

    @Test
    public void testStopViaAnnotation() throws InterruptedException, FailedToStopException {

        ArooaSession session = new StandardArooaSession();

        AnnotatedWaitingJob job = new AnnotatedWaitingJob();

        Optional<JobAdaptor> jobAdaptor = new JobStrategies().adapt(job, session);
        assertThat(jobAdaptor.isPresent(), is(true));

        Runnable proxy = (Runnable) new JobProxyGenerator().generate(
                jobAdaptor.get(),
                getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(new StandardArooaSession());

        IconSteps icons = new IconSteps((Iconic) proxy);
        icons.startCheck(IconHelper.READY, IconHelper.EXECUTING);

        Thread t = new Thread(proxy);

        t.start();

        icons.checkWait();

        icons.startCheck(IconHelper.EXECUTING,
                IconHelper.STOPPING, IconHelper.COMPLETE);

        ((Stoppable) proxy).stop();

        t.join();

        icons.checkNow();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));
    }

    private static final class StoppableWaitingJob implements Runnable, Stoppable {

        CountDownLatch barrier = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }

        @Override
        public void stop() {

            barrier.countDown();
        }
    }

    @Test
    public void testStopViaStoppable() throws InterruptedException, FailedToStopException {

        ArooaSession session = new StandardArooaSession();

        StoppableWaitingJob job = new StoppableWaitingJob();

        Optional<JobAdaptor> jobAdaptor = new JobStrategies().adapt(job, session);
        assertThat(jobAdaptor.isPresent(), is(true));

        Runnable proxy = (Runnable) new JobProxyGenerator().generate(
                jobAdaptor.get(),
                getClass().getClassLoader());

        ((ArooaSessionAware) proxy).setArooaSession(new StandardArooaSession());

        IconSteps icons = new IconSteps((Iconic) proxy);
        icons.startCheck(IconHelper.READY, IconHelper.EXECUTING);

        Thread t = new Thread(proxy);

        t.start();

        icons.checkWait();

        icons.startCheck(IconHelper.EXECUTING,
                IconHelper.STOPPING, IconHelper.COMPLETE);

        ((Stoppable) proxy).stop();

        t.join();

        icons.checkNow();

        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));
    }
}
