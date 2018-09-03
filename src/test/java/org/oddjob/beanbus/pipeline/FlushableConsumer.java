package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;

public interface FlushableConsumer<T> extends Consumer<T> {

    @Override
    void accept(T data);

    void flush();
}
