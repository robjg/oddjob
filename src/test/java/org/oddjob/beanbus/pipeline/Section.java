package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;

/**
 *
 * @param <T>
 * @param <U>
 */
@FunctionalInterface
public interface Section<T, U> {

    Pipe<T> linkTo(Consumer<? super U> next);
}
