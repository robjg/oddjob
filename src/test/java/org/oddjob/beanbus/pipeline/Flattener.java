package org.oddjob.beanbus.pipeline;

import java.util.Collection;

/**
 * Flatten batched data in a pipeline.
 *
 * @param <T> The type of the data
 */
public class Flattener<T> implements FlushableConsumer<Collection<? extends T>> {

    private final FlushableConsumer<? super T> next;

    public Flattener(FlushableConsumer<? super T> next) {
        this.next = next;
    }

    @Override
    public void accept(Collection<? extends T> data) {
        data.forEach(next);
    }

    @Override
    public void flush() {
        next.flush();
    }
}
