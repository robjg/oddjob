/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs.structural;

import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaElement;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.state.*;
import org.oddjob.values.types.SequenceIterable;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Rob Gordon.
 * @oddjob.description This job will repeatedly run its child job. The repeat
 * can be either for:
 * <ul>
 * 	<li>Each value of a collection.</li>
 *  <li>Or a given number times.</li>
 *  <li>Or until the until property is true.</li>
 * </ul>
 * <p>
 * Without either a until or a times or values the job will loop indefinitely.
 * @oddjob.example Repeat a job 3 times.
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/RepeatExample.xml}
 * @oddjob.example Repeat a job 3 times with a sequence.
 * <p>
 * {@oddjob.xml.resource org/oddjob/jobs/structural/RepeatWithSequence.xml}
 */
public class RepeatJob extends StructuralJob<Runnable>
        implements Stoppable {
    private static final long serialVersionUID = 20120121;

    /**
     * @oddjob.property
     * @oddjob.description Repeat will repeat until the value of
     * this property is true.
     * @oddjob.required No.
     */
    private volatile boolean until;

    /**
     * @oddjob.property
     * @oddjob.description The count of repeats.
     * @oddjob.required Read Only.
     */
    private final AtomicInteger count = new AtomicInteger();


    /**
     * @oddjob.property
     * @oddjob.description The number of times to repeat.
     * @oddjob.required No.
     */
    private volatile int times;

    /**
     * @oddjob.property
     * @oddjob.description Values to repeat over.
     * @oddjob.required No.
     */
    private transient volatile Iterable<?> values;

    /**
     * Current iterator.
     */
    private transient volatile Iterator<?> iterator;

    /**
     * @oddjob.property
     * @oddjob.description The current value of the repeat.
     * @oddjob.required Read Only.
     */
    private transient volatile Object current;

    /**
     * The executor to use.
     */
    private volatile transient ExecutorService executorService;

    /**
     * Watch execution to start the state reflector when all children
     * have finished, and track job threads.
     */
    private volatile transient CompletableFuture<?> completableFuture;

    @Override
    protected StateOperator getInitialStateOp() {
        return new AnyActiveStateOp();
    }

    /**
     * Set the {@link ExecutorService}.
     *
     * @param executorService The Executor Service.
     * @oddjob.property executorService
     * @oddjob.description The ExecutorService to use. This will
     * be automatically set by Oddjob.
     * @oddjob.required No.
     */
    @Inject
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Getter for executor service.
     *
     * @return The executor service or null if not set.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * @oddjob.property job
     * @oddjob.description The job who's execution
     * to schedule.
     * @oddjob.required Yes.
     */
    @ArooaComponent
    public void setJob(Runnable child) {
        if (child == null) {
            childHelper.removeChildAt(0);
        } else {
            if (childHelper.size() > 0) {
                throw new IllegalArgumentException("Child Job already set.");
            }
            childHelper.insertChild(0, child);
        }
    }

    private enum Mode {DONE, ASYNC, SYNC}

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jobs.AbstractJob#execute()
     */
    protected void execute() {

        Runnable job = childHelper.getChild();
        if (job == null) {
            return;
        }

        if (!(job instanceof Stateful)) {
            throw new IllegalArgumentException("Job must be Stateful");
        }

        if (iterator == null) {
            if (times > 0) {
                iterator = new SequenceIterable(1, times, 1).iterator();
            } else {
                if (values == null) {
                    iterator = null;
                } else {
                    iterator = values.iterator();
                }
            }
        }

        class RunChild implements Runnable {

            private final Runnable onDone;

            RunChild(Runnable onDone) {
                this.onDone = onDone;
            }

            @Override
            public void run() {
                if (!stop && !until && (iterator == null || iterator.hasNext())) {

                    count.incrementAndGet();
                    if (iterator != null) {
                        current = iterator.next();
                    }

                    ResetActions.AUTO.doWith(job);

                    job.run();

                } else {
                    onDone.run();
                }
            }
        }

        AtomicReference<Mode> mode = new AtomicReference<>(Mode.SYNC);

        StateListener syncListener = new StateListener() {
            @Override
            public void jobStateChange(StateEvent event) {
                if (StateConditions.LIVE.test(event)) {
                    ((Stateful) job).removeStateListener(this);
                    mode.set(Mode.ASYNC);
                } else if (StateConditions.FAILURE.test(event)) {
                    ((Stateful) job).removeStateListener(this);
                    RepeatJob.super.startChildStateReflector();
                    mode.set(Mode.DONE);
                }
            }
        };

        StateListener asyncListener = new StateListener() {

            private final Runnable doneAction = () -> {
                ((Stateful) job).removeStateListener(this);
                RepeatJob.super.startChildStateReflector();
            };

            private final RunChild asyncRun = new RunChild(doneAction);

            @Override
            public void jobStateChange(StateEvent event) {
                if (StateConditions.COMPLETE.test(event)) {
                    completableFuture = CompletableFuture.runAsync(
                            asyncRun, executorService);
                } else if (StateConditions.FAILURE.test(event)) {
                    doneAction.run();
                }
            }
        };

        ((Stateful) job).addStateListener(syncListener);

        RunChild runChild = new RunChild(() -> {
            RepeatJob.super.startChildStateReflector();
            mode.set(Mode.DONE);
        });

        loop:
        while (!stop) {
            switch (mode.get()) {
                case DONE:
                    break loop;
                case SYNC:
                    runChild.run();
                    continue;
                case ASYNC:
                    stateHandler().waitToWhen(StateConditions.ANY,
                            () -> getStateChanger().setState(ParentState.ACTIVE));

                    ((Stateful) job).addStateListener(asyncListener);
                    break loop;
            }
        }

        if (stop) {
            ((Stateful) job).removeStateListener(syncListener);
            super.startChildStateReflector();
        }

    }

    @Override
    protected void onStop() throws FailedToStopException {
        Optional.ofNullable(this.completableFuture)
                .ifPresent(cf ->
                        completableFuture.cancel(false));

        super.onStop();
    }

    @Override
    protected void startChildStateReflector() {
        // This is started by us so override and do nothing.
    }

    @Override
    protected void onHardReset() {
        iterator = null;
        count.set(0);
        until = false;
        completableFuture = null;
    }

    public void setValues(Iterable<?> values) {
        this.values = values;
    }

    public Iterable<?> getValues() {
        return values;
    }

    public boolean isUntil() {
        return until;
    }

    @ArooaElement
    public void setUntil(boolean until) {
        this.until = until;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getCount() {
        return count.get();
    }

    /**
     * @return The index.
     * @oddjob.property index
     * @oddjob.description The same as count. Provided so configurations
     * can be swapped between this and {@link ForEachJob} job.
     */
    public int getIndex() {
        return count.get();
    }

    public Object getCurrent() {
        return current;
    }
}
