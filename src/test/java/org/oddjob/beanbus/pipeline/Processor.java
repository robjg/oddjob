package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;

/**
 * Something that consumes data and then produces a final result.
 *
 * @param <T> The type of data to be processed.
 * @param <R> The type of the result.
 */
public interface Processor<T, R> extends Consumer<T> {

    /**
     * Complete processing and produce a result.
     *
     * @return The result.
     */
    R complete();
}
