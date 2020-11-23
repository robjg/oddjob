/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework.adapt.job;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.*;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.framework.AsyncJob;
import org.oddjob.framework.adapt.*;
import org.oddjob.framework.adapt.beanutil.WrapDynaBean;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Creates a proxy for any {@link java.lang.Runnable} to allow it to be controlled and
 * monitored within Oddjob.
 *
 * @author Rob Gordon.
 */
public class RunnableWrapper extends BaseWrapper
        implements ComponentWrapper, Serializable, Forceable {
    private static final long serialVersionUID = 20012052320051231L;

    /**
     * Handle state.
     */
    private transient volatile JobStateHandler stateHandler;

    /**
     * Used to notify clients of an icon change.
     */
    private transient volatile IconHelper iconHelper;

    /**
     * Perform the state change.
     */
    private transient volatile JobStateChanger stateChanger;

    /**
     * The wrapped Runnable.
     */
    private final Object wrapped;

    /**
     * The DynaBean that takes its properties of the wrapped Runnable.
     */
    private transient volatile DynaBean dynaBean;

    /**
     * The thread our job is executing on.
     */
    private transient volatile Thread thread;

    /**
     * The proxy we create that represents our wrapped Runnable within Oddjob.
     */
    private final Object proxy;

    /**
     * Reset with Interface or Annotations adaptor.
     */
    private transient volatile Resettable resettableAdaptor;

    /**
     * Stop with Interface or Annotations adaptor.
     */
    private transient volatile Stoppable stoppableAdaptor;

    /**
     * Async with Interface or Annotations adaptor.
     */
    private transient volatile AsyncJob asyncAdaptor;

    /**
     * Constructor.
     */
    public RunnableWrapper(Object wrapped, Object proxy) {
        this.wrapped = wrapped;
        this.proxy = proxy;
        completeConstruction();
    }

    /**
     * Complete construction. Called by constructor and post
     * deserialisation.
     */
    private void completeConstruction() {
        this.dynaBean = new WrapDynaBean(wrapped);
        stateHandler = new JobStateHandler((Stateful) proxy);
        iconHelper = new IconHelper(this,
                StateIcons.iconFor(stateHandler.getState()));
        stateChanger = new JobStateChanger(stateHandler,
                iconHelper,
                this::save);
    }

    @Override
    public void setArooaSession(ArooaSession session) {
        super.setArooaSession(session);

        resettableAdaptor = new ResetableAdaptorFactory().adapt(
                wrapped, session)
                .orElseGet(() -> new Resettable() {
                    @Override
                    public boolean softReset() {
                        return true;
                    }

                    @Override
                    public boolean hardReset() {
                        return true;
                    }
                });

        stoppableAdaptor = new StoppableAdaptorFactory().adapt(
                wrapped, session)
                .orElseGet(() ->
                        () -> stateHandler.runLocked(() -> {
                            Thread t = thread;
                            if (t != null) {
                                logger().info("Interrupting Thread [" + t.getName() +
                                        "] to attempt to stop job.");
                                t.interrupt();
                            } else {
                                logger().info("No Thread to interrupt. Hopefully Job has just stopped.");
                            }
                        })
                );

        asyncAdaptor = new AsyncAdaptorFactory()
                .adapt(wrapped, session)
                .orElse(null);
    }

    @Override
    protected IconHelper iconHelper() {
        return iconHelper;
    }

    @Override
    protected JobStateHandler stateHandler() {
        return stateHandler;
    }

    protected JobStateChanger getStateChanger() {
        return stateChanger;
    }

    @Override
    protected Object getWrapped() {
        return wrapped;
    }

    @Override
    protected DynaBean getDynaBean() {
        return dynaBean;
    }

    @Override
    protected Object getProxy() {
        return proxy;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if (!stateHandler.waitToWhen(
                new IsExecutable(),
                () -> getStateChanger().setState(JobState.EXECUTING))) {
            return;
        }

        logger().info("Executing.");

        final AtomicReference<Throwable> exception =
                new AtomicReference<>();
        final AtomicReference<Object> callableResult =
                new AtomicReference<>();

        thread = Thread.currentThread();
        try {
            configure();

            if (asyncAdaptor != null) {
                asyncAdaptor.acceptCompletionHandle(result ->
                        stateHandler.waitToWhen(new IsStoppable(), () -> {
                    if (result == 0) {
                        getStateChanger().setState(JobState.COMPLETE);
                    } else {
                        getStateChanger().setState(JobState.INCOMPLETE);
                    }
                }));
                asyncAdaptor.acceptExceptionListener(e ->
                        stateHandler.waitToWhen(new IsStoppable(), () ->
                                getStateChanger().setStateException(e)));
            }

            if (wrapped instanceof Callable<?>) {
                callableResult.set(((Callable<?>) wrapped).call());
            } else {
                ((Runnable) wrapped).run();
            }

        } catch (Throwable t) {
            logger().error("Exception:", t);
            exception.set(t);
        } finally {
            stateHandler.runLocked(() -> {
                if (Thread.interrupted()) {
                    logger().debug("Clearing thread interrupted flag.");
                }
                thread = null;
            });
        }

        if (asyncAdaptor == null) {
            logger().info("Finished.");

            stateHandler.waitToWhen(new IsStoppable(), () -> {
                if (exception.get() != null) {
                    getStateChanger().setStateException(exception.get());
                } else {
                    int result;
                    try {
                        result = getResult(callableResult.get());

                        if (result == 0) {
                            getStateChanger().setState(JobState.COMPLETE);
                        } else {
                            getStateChanger().setState(JobState.INCOMPLETE);
                        }
                    } catch (Exception e) {
                        // When will this ever happen?
                        getStateChanger().setStateException(e);
                    }
                }
            });
        }
        else {
            logger().info("Started async.");

            stateHandler.waitToWhen(new IsStoppable(), () ->
                    getStateChanger().setState(JobState.ACTIVE));
        }
    }

    @Override
    public void onStop() throws FailedToStopException {
        if (stoppableAdaptor == null) {
            throw new NullPointerException(
                    "StoppableAdaptor hasn't been set, " +
                            "setArooaSession() must be called on the proxy.");
        }
        stoppableAdaptor.stop();
    }

    /**
     * Perform a soft reset on the job.
     */
    @Override
    public boolean softReset() {
        return stateHandler.waitToWhen(new IsSoftResetable(), () -> {
            if (resettableAdaptor == null) {
                throw new NullPointerException(
                        "ResettableAdaptor hasn't been set, " +
                                "setArooaSession() must be called on the proxy.");
            }

            resettableAdaptor.softReset();

            getStateChanger().setState(JobState.READY);

            logger().info("Soft Reset complete.");
        });
    }

    /**
     * Perform a hard reset on the job.
     */
    @Override
    public boolean hardReset() {
        return stateHandler.waitToWhen(new IsHardResetable(), () -> {
            if (resettableAdaptor == null) {
                throw new NullPointerException(
                        "ResettableAdaptor hasn't been set, " +
                                "setArooaSession() must be called on the proxy.");
            }

            resettableAdaptor.hardReset();

            getStateChanger().setState(JobState.READY);

            logger().info("Hard Reset complete.");
        });
    }

    /**
     * Force the job to COMPLETE.
     */
    @Override
    public void force() {

        stateHandler.waitToWhen(new IsForceable(), () -> {
            logger().info("Forcing complete.");

            getStateChanger().setState(JobState.COMPLETE);
        });
    }

    /**
     * Custom serialisation.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(stateHandler.lastStateEvent().serializable());
    }

    /**
     * Custom serialisation.
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        StateEvent.SerializableNoSource savedEvent =
                (StateEvent.SerializableNoSource) s.readObject();
        completeConstruction();
        stateHandler.restoreLastJobStateEvent(savedEvent);
        iconHelper.changeIcon(StateIcons.iconFor(stateHandler.getState()));
    }

    /**
     * Internal method to fire state.
     */
    protected void fireDestroyedState() {

        if (!stateHandler().waitToWhen(new IsAnyState(), () -> {
            stateHandler().setState(JobState.DESTROYED);
            stateHandler().fireEvent();
        })) {
            throw new IllegalStateException("[" + RunnableWrapper.this + "] Failed set state DESTROYED");
        }

        logger().debug("[" + this + "] Destroyed.");
    }
}
