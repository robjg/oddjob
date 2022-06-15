/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.framework.adapt.service;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.framework.AsyncService;
import org.oddjob.framework.adapt.BaseWrapper;
import org.oddjob.framework.adapt.ComponentWrapper;
import org.oddjob.framework.adapt.ResettableAdaptorFactory;
import org.oddjob.framework.adapt.beanutil.WrapDynaBean;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.*;

import java.util.Optional;

/**
 * Wraps a Service object and adds state to it.
 * <p>
 *
 * @author Rob Gordon.
 */
public class ServiceWrapper extends BaseWrapper
        implements ComponentWrapper {

    /**
     * Handle state.
     */
    private final ServiceStateHandler stateHandler;

    /**
     * Used to notify clients of an icon change.
     */
    private final IconHelper iconHelper;

    /**
     * Perform state changes.
     */
    private final ServiceStateChanger stateChanger;

    private final ServiceAdaptor serviceAdaptor;

    private final Object wrapped;

    private final DynaBean dynaBean;

    private final Object proxy;

    /**
     * Reset with Interface or Annotations adaptor.
     */
    private transient volatile Resettable resettableAdaptor;

    /**
     * Create a new instance wrapping a service.
     *
     * @param serviceAdaptor The Service, via its adapter.
     * @param proxy   The proxy generated for it.
     */
    public ServiceWrapper(ServiceAdaptor serviceAdaptor, Object proxy) {
        this.serviceAdaptor = serviceAdaptor;
        this.proxy = proxy;
        this.wrapped = serviceAdaptor.getComponent();
        this.dynaBean = new WrapDynaBean(wrapped);

        stateHandler = new ServiceStateHandler(this);
        iconHelper = new IconHelper(this,
                StateIcons.iconFor(stateHandler.getState()));
        stateChanger = new ServiceStateChanger(stateHandler, iconHelper,
                this::save);
    }

    @Override
    public void setArooaSession(ArooaSession session) {
        super.setArooaSession(session);

        resettableAdaptor = new ResettableAdaptorFactory().adapt(
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
    }

    @Override
    protected ServiceStateHandler stateHandler() {
        return stateHandler;
    }

    @Override
    protected IconHelper iconHelper() {
        return iconHelper;
    }

    protected ServiceStateChanger getStateChanger() {
        return stateChanger;
    }

    @Override
    public Object getWrapped() {
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

    @Override
    protected void save(Object component) {
        // Services don't persist. Maybe they should.
    }

    public void run() {
        Optional<AsyncService> possiblyAsync = serviceAdaptor.asAsync();
        boolean isAsync = possiblyAsync.isPresent();

        if (!stateHandler.waitToWhen(new IsExecutable(),
                () -> getStateChanger().setState(isAsync ? ServiceState.INITIALISING : ServiceState.STARTING))) {
            return;
        }

        logger().info("Service Starting.");

        try {
            if (Thread.interrupted()) {
                throw new InterruptedException(
                        "Service Thread interrupt flag set. " +
                                "Please ensure previous jobs clean up after " +
                                "themselves to guarantee consistent behaviour.");
            }

            serviceAdaptor.acceptExceptionListener(this::exceptionFromComponent);

            configure();

            if (possiblyAsync.isPresent()) {
                AsyncService async = possiblyAsync.get();
                async.acceptCompletionHandle(this::setStateStarted);
                async.run();
            }
            else {
                serviceAdaptor.start();
                setStateStarted();
            }
        } catch (final Throwable t) {
            logger().error("Exception starting service:", t);

            stateHandler.waitToWhen(new IsAnyState(),
                    () -> getStateChanger().setStateException(t));
        }
    }

    protected void setStateStarted() {
        if (stateHandler.waitToWhen(StateConditions.RUNNING,
                () -> getStateChanger().setState(ServiceState.STARTED))) {
            logger().info("Service Started.");
        } else {
            logger().info("Service Stopped before Started.");
        }
    }

    /**
     * Used by the exception handler callback.
     *
     * @param e
     */
    protected void exceptionFromComponent(final Exception e) {
        logger().error("Exception From service:", e);

        if (!stateHandler.waitToWhen(new IsStoppable(),
                () -> getStateChanger().setStateException(e))) {
            throw new IllegalStateException("Service has not started.");
        }
    }

    @Override
    protected void onStop() throws FailedToStopException {

        try {
            serviceAdaptor.stop();

            stateHandler.waitToWhen(new IsAnyState(),
                    () -> getStateChanger().setState(ServiceState.STOPPED));
        } catch (Exception e) {
            throw new FailedToStopException(serviceAdaptor, e);
        }
    }

    /**
     * Perform a soft reset on the job.
     */
    public boolean softReset() {
        return stateHandler.waitToWhen(new IsSoftResetable(),
                () -> {
                    if (resettableAdaptor == null) {
                        throw new NullPointerException(
                                "ResettableAdaptor hasn't been set, " +
                                        "setArooaSession() must be called on the proxy.");
                    }

                    resettableAdaptor.softReset();

                    getStateChanger().setState(ServiceState.STARTABLE);

                    logger().info("Soft Reset complete.");
                });
    }

    /**
     * Perform a hard reset on the job.
     */
    public boolean hardReset() {

        return stateHandler.waitToWhen(new IsHardResetable(), () -> {
            if (resettableAdaptor == null) {
                throw new NullPointerException(
                        "ResettableAdaptor hasn't been set, " +
                                "setArooaSession() must be called on the proxy.");
            }

            resettableAdaptor.hardReset();

            getStateChanger().setState(ServiceState.STARTABLE);

            logger().info("Hard Reset complete.");
        });
    }

    /**
     * Internal method to fire state.
     */
    protected void fireDestroyedState() {

        if (!stateHandler().waitToWhen(new IsAnyState(), () -> {
            stateHandler().setState(ServiceState.DESTROYED);
            stateHandler().fireEvent();
        })) {
            throw new IllegalStateException("[" + ServiceWrapper.this + "] Failed set state DESTROYED");
        }
        logger().debug("[" + this + "] Destroyed.");
    }
}
