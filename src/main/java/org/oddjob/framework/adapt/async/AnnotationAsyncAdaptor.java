package org.oddjob.framework.adapt.async;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.utils.AnnotationFinder;
import org.oddjob.framework.AsyncJob;
import org.oddjob.framework.adapt.AcceptCompletionHandle;
import org.oddjob.framework.adapt.AcceptExceptionListener;
import org.oddjob.framework.adapt.AdaptorFactory;
import org.oddjob.util.OddjobWrapperException;

import java.beans.ExceptionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.IntConsumer;

/**
 * Helps to find (or not) the Async Annotations.
 *
 * @author Rob
 */
public class AnnotationAsyncAdaptor implements AdaptorFactory<AsyncJob> {

    private final Runnable run;

    public AnnotationAsyncAdaptor(Runnable run) {
        this.run = run;
    }

    @Override
    public Optional<AsyncJob> adapt(Object component, ArooaSession session) {
        ArooaAnnotations annotations = AnnotationFinder.forSession(session)
                .findFor(component.getClass());

        final Method acceptCompletionHandle =
                annotations.methodFor(AcceptCompletionHandle.class.getName());
        final Method acceptExceptionListener =
                annotations.methodFor(AcceptExceptionListener.class.getName());

        if (acceptCompletionHandle == null && acceptExceptionListener == null) {
            return Optional.empty();
        }

        return Optional.of(new AsyncJob() {

            @Override
            public void run() {
                run.run();
            }

            @Override
            public void acceptCompletionHandle(IntConsumer stopWithState) {
                invoke(component, acceptCompletionHandle, stopWithState);
            }

            @Override
            public void acceptExceptionListener(ExceptionListener exceptionListener) {
                invoke(component, acceptExceptionListener, exceptionListener);
            }

        });
    }

    private void invoke(Object component, Method m, Object arg) {
        if (m == null) {
            return;
        }

        try {
            m.invoke(component, arg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new OddjobWrapperException(e);
        }
    }
}
