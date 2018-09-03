package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BatchingConsumer<T> implements FlushableConsumer<T> {

    private final AtomicInteger counter = new AtomicInteger();

    private final Queue<T> queue = new ConcurrentLinkedQueue<>();

    private final FlushableConsumer<Collection<T>> next;
    private final int batchSize;

    public BatchingConsumer(FlushableConsumer<Collection<T>> next, int batchSize) {
        this.next = next;
        this.batchSize = batchSize;
    }

    @Override
    public void accept(T data) {
        Collection<T> batch = new ArrayList<>();
        if (counter.incrementAndGet() == batchSize) {
            for (int i = 0; i < batchSize; ++i) {
                batch.add(queue.remove());
            }
            next.accept(batch);
        }
    }

    @Override
    public void flush() {
        next.accept(queue.stream().collect(Collectors.toList()));
    }
}
