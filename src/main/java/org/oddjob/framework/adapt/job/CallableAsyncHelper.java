package org.oddjob.framework.adapt.job;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.framework.AsyncJob;

import java.beans.ExceptionListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

/**
 * Helps to see if a Callable is Asynchronous.
 */
public class CallableAsyncHelper {

    public static Optional<AsyncJob> adapt(Callable<?> callable, ArooaSession session) {

        if (CompletableFuture.class.isAssignableFrom(getCallableType((callable)))) {

            //noinspection unchecked
            return Optional.of(new AsyncCallableAdaptor((Callable<CompletableFuture<?>>) callable,
                    session.getTools().getArooaConverter()));
        }
        else {
            return Optional.empty();
        }
    }

    static class AsyncCallableAdaptor implements AsyncJob {

        private final Callable<CompletableFuture<?>> callable;

        private final ArooaConverter converter;

        private IntConsumer stopWithState;

        private ExceptionListener exceptionListener;

        AsyncCallableAdaptor(Callable<CompletableFuture<?>> callable, ArooaConverter converter) {
            this.callable = callable;
            this.converter = converter;
        }

        @Override
        public void run() {
            try {
                CompletableFuture<?> completableFuture = callable.call();

                completableFuture.whenComplete((v, t) -> {
                    if (t == null) {
                        try {
                            stopWithState.accept(converter.convert(v, Integer.class));
                        }
                        catch (ArooaConversionException e) {
                            exceptionListener.exceptionThrown(e);
                        }
                    }
                    else {
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

    static <T> Class<T> getCallableType(Callable<T> callable) {

        return getCallableTypeOf(callable.getClass());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static <T> Class<T> getCallableTypeOf(Class<? extends Callable> callableClass) {

        Optional<Type> callableInterfaceType = Arrays.stream(callableClass.getGenericInterfaces())
                .filter(c -> Callable.class == rawType(c))
                .findFirst();

        if (callableInterfaceType.isPresent()) {

            Type t = ((ParameterizedType) callableInterfaceType.get()).getActualTypeArguments()[0];

            //noinspection unchecked
            return (Class<T>) rawType(t);
        }
        else {

            return getCallableTypeOf((Class<Callable>)callableClass.getSuperclass());
        }
    }

    static Class<?> rawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        else if (type instanceof ParameterizedType){
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        else {
            throw new IllegalArgumentException("Can't work out raw type of [" + type + "]");
        }
    }
}
