package org.oddjob.framework.adapt.async;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.framework.AsyncJob;

import java.beans.ExceptionListener;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

/**
 * Check a method to see if it will provide an {@link AsyncJob}.
 */
public class MethodAsyncHelper {

    public static Optional<AsyncJob> adapt(Object component, Method method, ArooaSession session) {

        if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {

            return Optional.of(new AsyncMethodAdaptor(component, method,
                    session.getTools().getArooaConverter()));
        } else {
            return Optional.empty();
        }
    }

    static class AsyncMethodAdaptor implements AsyncJob {

        private final Object component;
        private final Method method;

        private final ArooaConverter converter;

        private IntConsumer stopWithState;

        private ExceptionListener exceptionListener;

        AsyncMethodAdaptor(Object component, Method method, ArooaConverter converter) {
            this.component = component;
            this.method = method;
            this.converter = converter;
        }

        @Override
        public void run() {
            try {
                CompletableFuture<?> completableFuture = (CompletableFuture<?>) method.invoke(component);

                completableFuture.whenComplete((v, t) -> {
                    if (t == null) {
                        try {
                            stopWithState.accept(converter.convert(v, Integer.class));
                        } catch (ArooaConversionException e) {
                            exceptionListener.exceptionThrown(e);
                        }
                    } else {
                        exceptionListener.exceptionThrown((Exception) t);
                    }
                });
            } catch (Exception e) {
                exceptionListener.exceptionThrown(e);
            }
        }

        @Override
        public void acceptCompletionHandle(IntConsumer stopWithState) {
            this.stopWithState = stopWithState;
        }

        @Override
        public void acceptExceptionListener(ExceptionListener exceptionListener) {
            this.exceptionListener = exceptionListener;
        }
    }

}
