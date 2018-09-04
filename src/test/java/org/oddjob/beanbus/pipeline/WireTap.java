package org.oddjob.beanbus.pipeline;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class WireTap<T> implements FlushableConsumer<T> {

    private final Collection<T> results = new ConcurrentLinkedQueue<>();

    private final Optional<FlushableConsumer<? super T>> next;

    public WireTap() {
        this(Optional.empty());
    }

    public WireTap(Optional<FlushableConsumer<? super T>> next) {
        this.next = next;
    }

    public Collection<T> toCollection() {
        return results.stream().collect(Collectors.toList());
    }

    @Override
    public void accept(T data) {
        results.add(data);
        next.ifPresent( n -> n.accept(data));
    }

    @Override
    public void flush() {
        next.ifPresent( n -> n.flush());
    }

}
