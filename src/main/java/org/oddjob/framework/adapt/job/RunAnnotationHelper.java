package org.oddjob.framework.adapt.job;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.framework.AsyncJob;
import org.oddjob.framework.adapt.Run;
import org.oddjob.framework.adapt.async.AnnotationAsyncAdaptor;
import org.oddjob.framework.adapt.async.MethodAsyncHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Allows any component annotated with {@link Run} to be a job.
 */
public class RunAnnotationHelper {

    public static Optional<JobAdaptor> jobFromAnnotation(Object component,
                                                         ArooaSession session) {

        PropertyAccessor accessor =
                session.getTools().getPropertyAccessor();

        ArooaBeanDescriptor beanDescriptor =
                session.getArooaDescriptor().getBeanDescriptor(
                        accessor.getClassName(component), accessor);

        ArooaAnnotations annotations =
                beanDescriptor.getAnnotations();

        Method runMethod = annotations.methodFor(
                Run.class.getName());

        if (runMethod == null) {
            return Optional.empty();
        }

        return Optional.of(new MethodAdaptor(
                component, runMethod));
    }

    static class MethodAdaptor implements JobAdaptor {

        private final Object component;

        private final Method method;

        private volatile transient ArooaSession session;

        MethodAdaptor(Object component, Method method) {
            this.component = Objects.requireNonNull(component);
            this.method = method;
        }

        @Override
        public void setArooaSession(ArooaSession session) {
            this.session = session;
        }

        @Override
        public Integer call() throws Exception {

            Object callResult = method.invoke(component);

            ArooaConverter converter = session.getTools().getArooaConverter();

            return converter.convert(callResult, Integer.class);
        }

        @Override
        public Object getComponent() {
            return component;
        }

        @Override
        public Optional<AsyncJob> asAsync() {

            Optional<AsyncJob> maybeAsync = MethodAsyncHelper.adaptJob(component, method, session);
            if (maybeAsync.isPresent())  {
                return maybeAsync;
            }

            return new AnnotationAsyncAdaptor(() -> {
                try {
                    method.invoke(component);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }).adapt(component, session);
        }
    }

}
