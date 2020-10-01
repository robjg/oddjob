package org.oddjob.framework.adapt;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.utils.AnnotationFinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapt a component to an {@link Stoppable} if possible.
 */
public class StoppableAdaptorFactory implements AdaptorFactory<Stoppable> {

    @Override
    public Optional<Stoppable> adapt(Object component, ArooaSession session) {
        Objects.requireNonNull(component);

        if (component instanceof Stoppable) {
            return Optional.of((Stoppable) component);
        }

        ArooaAnnotations annotations = AnnotationFinder.forSession(session)
                .findFor(component.getClass());

        final Method stopMethod =
                annotations.methodFor(Stop.class.getName());

        if (stopMethod == null) {
            return Optional.empty();
        }

        return Optional.of(() -> {
            try {
                stopMethod.invoke(component);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new FailedToStopException(component, e);
            }
        });
    }
}
