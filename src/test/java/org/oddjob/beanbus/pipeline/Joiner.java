package org.oddjob.beanbus.pipeline;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Joiner<T>{

    private final AtomicInteger counter = new AtomicInteger();

    private final FlushableConsumer<? super T> next;

    private final Queue<T> saved = new ConcurrentLinkedQueue<>();

    public Joiner(FlushableConsumer<? super T> next) {
        this.next = next;
    }

    public FlushableConsumer<T> newJoinPont() {

        counter.incrementAndGet();

        return new FlushableConsumer<T>() {
            @Override
            public void accept(T data) {
                saved.add(data);
            }

            @Override
            public void flush() {
                if (counter.decrementAndGet() == 0) {
                    saved.forEach(next::accept);
                    next.flush();
                }
            }
        };
    }


}
