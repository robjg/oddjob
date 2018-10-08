package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @param <T>
 */
public class Batcher<T> implements Section<T, List<T>> {

    private final int batchSize;

    public Batcher(int batchSize) {
        this.batchSize = batchSize;
    }

    public static <X> Section<X, List<X>> ofSize(int batchSize) {
        return new Batcher<>(batchSize);
    }

    @Override
    public Pipe<T> linkTo(Consumer<? super List<T>> next) {
        return new Pipe<T>() {

            private final AtomicInteger counter = new AtomicInteger();

            private final Queue<T> queue = new ConcurrentLinkedQueue<>();

            @Override
            public void accept(T data) {
                queue.add(data);
                if (counter.incrementAndGet() % batchSize == 0) {
                    List<T> batch = new ArrayList<>();
                    for (int i = 0; i < batchSize; ++i) {
                        batch.add(queue.remove());
                    }
                    next.accept(batch);
                }
            }

            @Override
            public void flush() {
                List<T> remaining = queue.stream().collect(Collectors.toList());
                if (!remaining.isEmpty()) {
                    next.accept(remaining);
                }
            }

        };
    }
}
