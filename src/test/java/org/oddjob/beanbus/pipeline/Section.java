package org.oddjob.beanbus.pipeline;

import org.oddjob.beanbus.pipeline.Pipe;

import java.util.function.Consumer;

@FunctionalInterface
public interface Section<T, U> {

    Pipe<T> linkTo(Consumer<? super U> next);
}
