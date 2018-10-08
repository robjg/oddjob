package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Map data in a pipeline using the given function.
 *
 * @param <T> The type of incoming data.
 * @param <U> The type of outgoing data.
 */
public class Mapper<T, U> implements Section<T, U> {

    private final Function<? super T, ? extends U> mapping;

    public Mapper(Function<? super T, ? extends U> mapping) {
        this.mapping = mapping;
    }

    public static <T, U> Section<T, U> with(Function<? super T, ? extends  U> mapping) {
        return new Mapper<>(mapping);
    }

    @Override
    public Pipe<T> linkTo(Consumer<? super U> next) {
        return new Pipe<T>() {

            @Override
            public void accept(T data) {
                next.accept(mapping.apply(data));
            }

            @Override
            public void flush() {
            }
        };
    }

    public static <X> Section<X, X> identity() {
        return new Mapper<>(Function.identity());
    }
}