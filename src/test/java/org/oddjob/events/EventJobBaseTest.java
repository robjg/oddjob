package org.oddjob.events;

import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.beanbus.Outbound;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.state.*;
import org.oddjob.tools.StateSteps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;


public class EventJobBaseTest {

    static class OurEvents extends EventJobBase<String> {

        ParentState onChildComplete = ParentState.COMPLETE;

        String immediateEvent;
        Object job;
        Executor executor;
        String laterEvent;

        @Override
        protected void onImmediateEvent(String event) {
            this.immediateEvent = event;
        }

        @Override
        protected void onSubscriptionStarted(Object job, Executor executor) {
            this.job = job;
            this.executor = executor;
        }

        @Override
        protected  void onLaterEvent(String event, Object job, Executor executor) {
            this.laterEvent = event;
            assertThat(this.job, sameInstance(job));
            assertThat(this.executor, sameInstance(executor));
        }

        @Override
        protected StateListener stateOnChildComplete() {

            return event -> {
                if (StateConditions.FAILURE.test(event.getState())) {
                    unsubscribe();
                    switchToChildStateReflector();
                }
                else if (StateConditions.SUCCESS.test(event.getState())) {
                    stateHandler().runLocked(() -> getStateChanger().setState(onChildComplete));
                }
            };
        }
    }

    static class OurJob implements Runnable, Stateful {

        private final JobState state;

        OurJob() {
            this(JobState.COMPLETE);
        }


        OurJob(JobState jobState) {
            this.state = jobState;
        }

        final List<StateListener> listeners = new ArrayList<>();

        @Override
        public void run() {
            List<StateListener> copy = new ArrayList<>(listeners);
            copy.forEach(l -> l.jobStateChange(new StateEvent(this, this.state)));
        }

        @Override
        public void addStateListener(StateListener listener) throws JobDestroyedException {
            assertThat(this.listeners.contains(listener), is(false));
            this.listeners.add(listener);
            listener.jobStateChange(new StateEvent(this, JobState.READY));
        }

        @Override
        public void removeStateListener(StateListener listener) {
            assertThat(this.listeners.remove(listener), is(true));
        }

        @Override
        public StateEvent lastStateEvent() {
            return null;
        }
    }

    public static class OurDestination implements Outbound, Stoppable {

        private Consumer<? super String>  consumer;

        @Override
        public void stop() throws FailedToStopException {
            this.consumer = null;
        }

        @Override
        public void setTo(Consumer destination) {

            assertThat(this.consumer, is(nullValue()));
            this.consumer = destination;
        }
    }


    @Test
    public void testSimpleLifecycle() {

        Queue<Runnable> executions = new LinkedList<>();
        ExecutorService executorService = mock(ExecutorService.class);

        Future<?> future = mock(Future.class);
        doAnswer(invocation -> {
            executions.add(invocation.getArgument(0, Runnable.class));
            return future;
        }).when(executorService).submit(Mockito.any(Runnable.class));

        OurDestination source = new OurDestination();

        OurJob job = new OurJob();

        OurEvents ourEvents = new OurEvents();
        ourEvents.setExecutorService(executorService);
        ourEvents.setJobs(0, source);
        ourEvents.setJobs(1, job);

        StateSteps steps = new StateSteps(ourEvents);
        steps.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        ourEvents.run();

        steps.checkNow();

        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, nullValue());
        assertThat(ourEvents.job, sameInstance(job));

        steps.startCheck(ParentState.ACTIVE);

        source.consumer.accept("Bang");

        steps.checkNow();
        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, is("Bang"));

        steps.startCheck(ParentState.ACTIVE, ParentState.COMPLETE);

        ourEvents.executor.execute(job);

        executions.poll().run();

        steps.checkNow();

        assertThat(job.listeners.size(), is(2));

        steps.startCheck(ParentState.COMPLETE);

        ourEvents.unsubscribe();
        ourEvents.switchToChildStateReflector();

        steps.checkNow();
        ourEvents.hardReset();

        steps.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        ourEvents.run();

        steps.checkNow();
        assertThat(job.listeners.size(), is(2));

    }

    @Test
    public void testIncompleteJobWithEventSourceProperty() {

        Queue<Runnable> executions = new LinkedList<>();
        ExecutorService executorService = mock(ExecutorService.class);

        Future<?> future = mock(Future.class);
        doAnswer(invocation -> {
            executions.add(invocation.getArgument(0, Runnable.class));
            return future;
        }).when(executorService).submit(Mockito.any(Runnable.class));

        OurJob job = new OurJob(JobState.INCOMPLETE);

        AtomicReference<Consumer<? super String>> consumerRef = new AtomicReference<>();
        EventSource<String> eventSource = consumer -> {
            consumerRef.set(consumer);
            return () -> consumerRef.set(null);
        };

        OurEvents ourEvents = new OurEvents();
        ourEvents.setExecutorService(executorService);
        ourEvents.setEventSource(eventSource);
        ourEvents.setJobs(0, job);

        StateSteps steps = new StateSteps(ourEvents);
        steps.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        ourEvents.run();

        steps.checkNow();

        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, nullValue());
        assertThat(ourEvents.job, sameInstance(job));

        steps.startCheck(ParentState.ACTIVE);

        consumerRef.get().accept("Bang");

        steps.checkNow();
        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, is("Bang"));

        assertThat(job.listeners.size(), is(2));

        steps.startCheck(ParentState.ACTIVE, ParentState.INCOMPLETE);

        ourEvents.executor.execute(job);

        executions.poll().run();

        steps.checkNow();

        assertThat(job.listeners.size(), is(1));

        ourEvents.hardReset();

        steps.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        ourEvents.run();

        steps.checkNow();
        assertThat(job.listeners.size(), is(2));

    }

    @Test
    public void testAsDestination() throws FailedToStopException {

        Queue<Runnable> executions = new LinkedList<>();
        ExecutorService executorService = mock(ExecutorService.class);

        Future<?> future = mock(Future.class);
        doAnswer(invocation -> {
            executions.add(invocation.getArgument(0, Runnable.class));
            return future;
        }).when(executorService).submit(Mockito.any(Runnable.class));

        OurJob job = new OurJob();

        OurEvents ourEvents = new OurEvents();
        ourEvents.onChildComplete = ParentState.STARTED;
        ourEvents.setExecutorService(executorService);
        ourEvents.setBeDestination(true);
        ourEvents.setJobs(0, job);

        StateSteps steps = new StateSteps(ourEvents);
        steps.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        ourEvents.run();

        steps.checkNow();

        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, nullValue());
        assertThat(ourEvents.job, sameInstance(job));

        steps.startCheck(ParentState.ACTIVE);

        ourEvents.accept("Bang");

        steps.checkNow();
        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, is("Bang"));

        steps.startCheck(ParentState.ACTIVE, ParentState.STARTED);

        ourEvents.executor.execute(job);

        executions.poll().run();

        steps.checkNow();

        assertThat(job.listeners.size(), is(2));

        steps.startCheck(ParentState.STARTED, ParentState.ACTIVE, ParentState.STARTED);

        ourEvents.accept("Bong");

        ourEvents.executor.execute(job);

        executions.poll().run();

        steps.checkNow();
        assertThat(ourEvents.immediateEvent, nullValue());
        assertThat(ourEvents.laterEvent, is("Bong"));

        steps.startCheck(ParentState.STARTED, ParentState.COMPLETE);

        ourEvents.stop();

        steps.checkNow();

        assertThat(job.listeners.size(), is(1));
    }

    @Test
    public void testTriggerThenManualStop() throws FailedToStopException {

        Queue<Runnable> executions = new LinkedList<>();
        ExecutorService executorService = mock(ExecutorService.class);

        Future<?> future = mock(Future.class);
        doAnswer(invocation -> {
            executions.add(invocation.getArgument(0, Runnable.class));
            return future;
        }).when(executorService).submit(Mockito.any(Runnable.class));

        OurDestination source = new OurDestination();

        OurJob job = new OurJob();

        OurEvents ourEvents = new OurEvents();
        ourEvents.onChildComplete = ParentState.EXECUTING;
        ourEvents.setExecutorService(executorService);
        ourEvents.setJobs(0, source);
        ourEvents.setJobs(1, job);

        StateSteps steps = new StateSteps(ourEvents);
        steps.startCheck(ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        ourEvents.run();

        steps.checkNow();

        source.consumer.accept("Bang");

        steps.startCheck(ParentState.ACTIVE, ParentState.READY);

        ourEvents.executor.execute(job);

        ourEvents.stop();

        verify(future, times(1)).cancel(false);

        steps.checkNow();
    }
}