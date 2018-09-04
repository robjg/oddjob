package org.oddjob.beanbus.pipeline;

import java.util.function.Function;

/**
 * Map data in a pipeline using the given function.
 *
 * @param <T> The type of incoming data.
 * @param <U> The type of outgoing data.
 */
public class Mapper<T, U> implements FlushableConsumer<T> {

    private final FlushableConsumer<U> next;

    private final Function<T, U> mapping;

    public Mapper(FlushableConsumer<U> next, Function<T, U> mapping) {
        this.next = next;
        this.mapping = mapping;
    }

    @Override
    public void accept(T data) {
        next.accept(mapping.apply(data));
    }

    @Override
    public void flush() {
        next.flush();
    }
}
