package org.oddjob.beanbus.pipeline;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class CollectionConsumer<T> implements FlushableConsumer<T> {

    private final Collection<T> results = new ConcurrentLinkedQueue<>();

    public Collection<T> toCollection() {
        return results.stream().collect(Collectors.toList());
    }

    @Override
    public void accept(T data) {
        results.add(data);
    }

    @Override
    public void flush() {
    }

}
