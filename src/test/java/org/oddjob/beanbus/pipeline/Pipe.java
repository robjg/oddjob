package org.oddjob.beanbus.pipeline;

import java.io.Flushable;
import java.util.function.Consumer;

/**
 * A Consumer of data that can be flushed.
 *
 * @param <T> The type of data.
 */
@FunctionalInterface
public interface Pipe<T> extends Consumer<T>, Flushable {

    @Override
    default void flush() {}
}
