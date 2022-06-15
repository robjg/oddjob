package org.oddjob.framework.adapt.job;

import org.oddjob.Reserved;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.framework.AsyncJob;
import org.oddjob.framework.adapt.AdaptorFactory;
import org.oddjob.framework.adapt.async.AnnotationAsyncAdaptor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * A collection of different strategies that are applied to a component
 * to see if it can be adapted to a {@link Runnable}.
 *
 * @author rob
 */
public class JobStrategies implements AdaptorFactory<JobAdaptor> {

    @Override
    public Optional<JobAdaptor> adapt(Object component, ArooaSession session) {

        if (component instanceof Runnable) {
            return Optional.of(new RunnableAdaptor((Runnable) component));
        }
        else if (component instanceof Callable) {
            return Optional.of(new CallableAdaptor((Callable<?>) component));
        }
        else {
            return RunAnnotationHelper.jobFromAnnotation(component, session);
        }
    }

    static class RunnableAdaptor implements JobAdaptor {

        private final Runnable component;

        private volatile transient ArooaSession session;

        RunnableAdaptor(Runnable component) {
            this.component = Objects.requireNonNull(component);
        }

        @Override
        public void setArooaSession(ArooaSession session) {
            this.session = session;
        }

        @Override
        public Integer call() throws Exception {

            component.run();

            PropertyAccessor accessor = session.getTools().getPropertyAccessor();
            BeanOverview overview = accessor.getBeanOverview(component.getClass());

            if (!overview.hasReadableProperty(
                    Reserved.RESULT_PROPERTY)) {
                return 0;
            }

            ArooaConverter converter = session.getTools().getArooaConverter();

            return converter.convert(accessor.getProperty(component, Reserved.RESULT_PROPERTY),
                    Integer.class);
        }

        @Override
        public Object getComponent() {
            return component;
        }

        @Override
        public Optional<AsyncJob> asAsync() {

            if (component instanceof AsyncJob) {
                return Optional.of((AsyncJob) component);
            }


            return new AnnotationAsyncAdaptor(component)
                    .adapt(component, session);
        }
    }

    static class CallableAdaptor implements JobAdaptor {

        private final Callable<?> component;

        private volatile transient ArooaSession session;

        CallableAdaptor(Callable<?> component) {
            this.component = Objects.requireNonNull(component);
        }

        @Override
        public void setArooaSession(ArooaSession session) {
            this.session = session;
        }

        @Override
        public Integer call() throws Exception {

            Object callResult = component.call();

            ArooaConverter converter = session.getTools().getArooaConverter();

            return converter.convert(callResult, Integer.class);
        }

        @Override
        public Object getComponent() {
            return component;
        }

        @Override
        public Optional<AsyncJob> asAsync() {

            return CallableAsyncHelper.adapt(component, session);
        }
    }
}
