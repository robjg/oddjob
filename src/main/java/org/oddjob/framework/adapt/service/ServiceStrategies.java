package org.oddjob.framework.adapt.service;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.framework.AsyncService;
import org.oddjob.framework.FallibleComponent;
import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.AcceptExceptionListener;
import org.oddjob.framework.adapt.AdaptorFactory;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;

import java.beans.ExceptionListener;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A collection of different strategies that are applied to a component
 * to see if it can be adapted to a {@link Service}.
 *
 * @author rob
 */
public class ServiceStrategies implements ServiceStrategy, AdaptorFactory<ServiceAdaptor> {

    @Override
    public Optional<ServiceAdaptor> adapt(Object component, ArooaSession session) {

        return Optional.ofNullable(serviceFor(component, session));
    }

    @Override
    public ServiceAdaptor serviceFor(Object component,
                                     ArooaSession session) {

        ServiceAdaptor adaptor =
                new IsServiceAlreadyStrategy().serviceFor(
                        component, session);

        if (adaptor == null) {
            adaptor = new HasServiceAnnotationsStrategy().serviceFor(
                    component, session);
        }

        if (adaptor == null) {
            adaptor = new HasServiceMethodsStrategy().serviceFor(
                    component, session);
        }

        return adaptor;
    }

    /**
     * Provides a strategy that checks to see if the component is a
     * {@link Service} already.
     */
    public static class IsServiceAlreadyStrategy implements ServiceStrategy {

        @Override
        public ServiceAdaptor serviceFor(Object component,
                                         ArooaSession session) {

            if (component instanceof Service) {
                final Service service = (Service) component;
                return new ServiceAdaptor() {

                    @Override
                    public void start() throws Exception {
                        service.start();
                    }

                    @Override
                    public void stop() throws FailedToStopException {
                        service.stop();
                    }

                    @Override
                    public Object getComponent() {
                        return service;
                    }

                    @Override
                    public void acceptExceptionListener(
                            ExceptionListener exceptionListener) {
                        if (service instanceof FallibleComponent) {
                            ((FallibleComponent) service
                            ).acceptExceptionListener(
                                    exceptionListener);
                        }
                    }

                    @Override
                    public Optional<AsyncService> asAsync() {
                        return Optional.empty();
                    }
                };
            } else {
                return null;
            }
        }
    }

    public static class HasServiceMethodsStrategy implements ServiceStrategy {

        @Override
        public ServiceMethodAdaptor serviceFor(Object component,
                                               ArooaSession session) {
            Class<?> cl = component.getClass();
            try {
                Method startMethod = cl.getDeclaredMethod(
                        "start");
                if (startMethod.getReturnType() != Void.TYPE) {
                    return null;
                }
                Method stopMethod = cl.getDeclaredMethod(
                        "stop");
                if (stopMethod.getReturnType() != Void.TYPE) {
                    return null;
                }
                return new ServiceMethodAdaptor(
                        component, startMethod, stopMethod);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class HasServiceAnnotationsStrategy
            implements ServiceStrategy {

        @Override
        public ServiceAdaptor serviceFor(Object component,
                                         ArooaSession session) {

            PropertyAccessor accessor =
                    session.getTools().getPropertyAccessor();

            ArooaBeanDescriptor beanDescriptor =
                    session.getArooaDescriptor().getBeanDescriptor(
                            accessor.getClassName(component), accessor);

            ArooaAnnotations annotations =
                    beanDescriptor.getAnnotations();

            Method startMethod = annotations.methodFor(
                    Start.class.getName());
            Method stopMethod = annotations.methodFor(
                    Stop.class.getName());
            Method exceptionMethod = annotations.methodFor(
                    AcceptExceptionListener.class.getName());

            if (startMethod == null) {
                return null;
            }
            if (stopMethod == null) {
                throw new IllegalStateException(
                        "Class " + component.getClass().getName() +
                                " must have both @Stop method if it has a @Start method annotated.");
            }

            return new ServiceMethodAdaptor(
                    component, startMethod, stopMethod, exceptionMethod);
        }
    }
}
