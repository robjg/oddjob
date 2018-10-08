package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;

public interface Processor<T, R> extends Consumer<T> {

    R complete();
}
