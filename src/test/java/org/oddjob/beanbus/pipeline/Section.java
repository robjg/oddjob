package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;

/**
 * A component in a pipeline.
 *
 * @param <T> The type of data the section accepts.
 * @param <U> The type of data the section will pass to the next component.
 */
@FunctionalInterface
public interface Section<T, U> {

    /**
     * Create a pipe by linking to the next component in the pipeline.
     *
     * @param next The next component.
     *
     * @return A pipe that can be used by a pipeline to process data.
     */
    Pipe<T> linkTo(Consumer<? super U> next);
}
