/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.state.*;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Rob Gordon.
 * @oddjob.description Runs a job when triggered by the arrival of an event. The job will be re-run every time
 * the event arrives. If the job is still running when a new event arrives, the job will attempt to be stopped
 * and rerun. A typical use case would be processing a file when it arrives, but which may be re-sent with more
 * up-to-date information.
 * @oddjob.example Evaluating greengrocer portfolios of fruit when data arrives.
 * <p>
 * {@oddjob.xml.resource org/oddjob/events/example/PricingWhenExample.xml}
 * @oddjob.example Being a destination in a pipeline.
 * <p>
 * {@oddjob.xml.resource org/oddjob/events/WhenAsDestinationExample.xml}
 */
public class When<T> extends EventJobBase<T> {

    private static final long serialVersionUID = 2018060600L;

    /**
     * @oddjob.description The State Condition of the child job on which to halt event subscription.
     * @oddjob.required No. Defaults to FAILURE, i.e. an EXCEPTION or INCOMPLETE state.
     */
    private volatile StateCondition haltOn;

    /**
     * @oddjob.description How to handle triggers before the child job has completed. Built in options
     * are currently STOP_AND_RERUN and QUEUE.
     * @oddjob.required No. Defaults to STOP_AND_RERUN.
     *
     */
    private volatile TriggerStrategy triggerStrategy;

    /** Set during startup. */
    private volatile TriggerStrategy currentStrategy;

    /** Queue of outstanding events */
    private final ConcurrentLinkedQueue<T> eventQueue = new ConcurrentLinkedQueue<>();

    public interface TriggerContext<T> {

        Queue<T> getEventQueue();

        void setTrigger(T event);

        Object getJob();

        Executor getExecutor();
    }

    public interface TriggerStrategy {

        <T> void start(TriggerContext<T> context);

        <T> void trigger(TriggerContext<T> context);
    }

    public enum TriggerStrategies implements TriggerStrategy {

        STOP_AND_RERUN {
            @Override
            public <T> void start(TriggerContext<T> context) {
                if (!context.getEventQueue().isEmpty()) {
                    context.getExecutor().execute(
                            new StopAndRerunExecution<>(context.getJob(),
                                    context.getEventQueue(), context::setTrigger));
                }
            }

            @Override
            public <T> void trigger(TriggerContext<T> context) {

                context.getExecutor().execute(
                        new StopAndRerunExecution<>(context.getJob(),
                                context.getEventQueue(), context::setTrigger));
            }
        },

        QUEUE {

            QueuedExecution<?> lastFuture;

            <T> void chain(QueuedExecution<T> next, Executor executor) {
                if (lastFuture == null) {
                    executor.execute(next);
                } else {
                    lastFuture.thenRunAsync(next, executor);
                }
                lastFuture = next;
            }

            @Override
            public <T> void start(TriggerContext<T> context) {

                Object job = context.getJob();
                if (!(job instanceof Stateful)) {
                    throw new IllegalStateException("Job must be Stateful");
                }
                while (true) {
                    T event = context.getEventQueue().poll();
                    if (event == null) {
                        break;
                    }
                    chain(new QueuedExecution<>(
                                    (Stateful) job, event,
                                    context::setTrigger),
                            context.getExecutor());
                }
            }

            @Override
            public <T> void trigger(TriggerContext<T> context) {
                T event = context.getEventQueue().poll();
                if (event == null) {
                    throw new IllegalStateException("Trigger should not be called without an event in the queue");
                }
                chain(new QueuedExecution<>(
                                (Stateful) context.getJob(), event,
                                context::setTrigger),
                        context.getExecutor());
            }
        }
    }

    class Context implements TriggerContext<T> {

        final Object job;

        final Executor executor;

        Context(Object job, Executor executor) {
            this.job = job;
            this.executor = executor;
        }

        @Override
        public Queue<T> getEventQueue() {
            return eventQueue;
        }

        @Override
        public void setTrigger(T event) {
            When.this.setTrigger(event);
        }

        @Override
        public Object getJob() {
            return job;
        }

        @Override
        public Executor getExecutor() {
            return executor;
        }
    }

    /**
     * The Conversion from String
     */
    public static class Conversions implements ConversionProvider {
        @Override
        public void registerWith(ConversionRegistry registry) {
            registry.register(String.class, TriggerStrategy.class,
                    from -> TriggerStrategies.valueOf(from.toUpperCase()));
        }
    }


    @Override
    protected void onImmediateEvent(T event) {
        eventQueue.add(event);
    }

    @Override
    protected synchronized void onSubscriptionStarted(Object job, Executor executor) {

        this.currentStrategy = getTriggerStrategy();
        this.currentStrategy.start(new Context(job, executor));
    }

    @Override
    synchronized protected void onLaterEvent(T event, Object job, Executor executor) {

        eventQueue.add(event);
        this.currentStrategy.trigger(new Context(job, executor));
    }


    @Override
    protected StateListener stateOnChildComplete() {

        return event -> {
            if (getHaltOn().test(event.getState())) {
                unsubscribe();
                switchToChildStateReflector();
            } else if (StateConditions.FINISHED.test(event.getState())) {
                stateHandler().runLocked(() -> getStateChanger().setState(ParentState.STARTED));
            }
        };
    }

    public TriggerStrategy getTriggerStrategy() {
        return Objects.requireNonNullElse(triggerStrategy, TriggerStrategies.STOP_AND_RERUN);
    }

    @ArooaAttribute
    public void setTriggerStrategy(TriggerStrategy triggerStrategy) {
        this.triggerStrategy = triggerStrategy;
    }

    public StateCondition getHaltOn() {
        return Objects.requireNonNullElse(haltOn, StateConditions.FAILURE);
    }

    @ArooaAttribute
    public void setHaltOn(StateCondition haltOn) {
        this.haltOn = haltOn;
    }

    static class StopAndRerunExecution<T> implements Runnable {

        private final Object job;

        private final Queue<? extends T> eventQueue;

        private final Consumer<? super T> eventSetter;


        StopAndRerunExecution(Object job,
                              Queue<? extends T> eventQueue,
                              Consumer<? super T> eventSetter) {
            this.job = job;
            this.eventQueue = eventQueue;
            this.eventSetter = eventSetter;
        }

        @Override
        public void run() {

            T event = null;
            while (true) {
                T nextEvent = eventQueue.poll();
                if (nextEvent == null) {
                    break;
                } else {
                    event = nextEvent;
                }
            }

            // Two rapid events would cause two executions. Only one would end up with the last event.
            if (event == null) {
                return;
            }

            eventSetter.accept(event);

            if (job instanceof Stoppable) {
                try {
                    ((Stoppable) job).stop();
                } catch (FailedToStopException e) {
                    throw new RuntimeException("[" + this + "] failed to stop child [" + job + "]", e);
                }
            }

            if (job instanceof Resettable) {
                ((Resettable) job).hardReset();
            }


            if (job instanceof Runnable) {
                ((Runnable) job).run();
            }
        }

        @Override
        public String toString() {
            return "Immediate Execution for job [" + this.job + "], current event queue " + eventQueue;
        }
    }

    /**
     * Wrapper class for {@link TriggerStrategies#QUEUE} that chains Completable Futures which complete when
     * the job state completes.
     *
     * @param <T> The event type
     */
    static class QueuedExecution<T> extends CompletableFuture<Void> implements Runnable {

        private final Stateful job;

        private final T event;

        private final Consumer<? super T> eventSetter;

        QueuedExecution(Stateful job, T event, Consumer<? super T> eventSetter) {
            this.job = job;
            this.event = event;
            this.eventSetter = eventSetter;
        }

        @Override
        public void run() {

            job.addStateListener(new StateListener() {
                @Override
                public void jobStateChange(StateEvent event) {
                    if (StateConditions.ENDED.test(event)) {
                        job.removeStateListener(this);
                        QueuedExecution.this.complete(null);
                    }
                }
            });

            if (job instanceof Resettable) {
                ((Resettable) job).hardReset();
            }

            eventSetter.accept(event);

            if (job instanceof Runnable) {
                ((Runnable) job).run();
            }
        }

        @Override
        public String toString() {
            return "Queued Execution for job [" + this.job + "] running with event [" + this.event + "]";
        }
    }
}
