package org.oddjob.beanbus.pipeline;

import java.util.concurrent.atomic.AtomicInteger;

public class JoinConsumer<T>{

    private final AtomicInteger counter = new AtomicInteger();

    private final FlushableConsumer<T> next;

    public JoinConsumer(FlushableConsumer<T> next) {
        this.next = next;
    }


    public FlushableConsumer<T> register(FlushableConsumer<T> joinOn) {

        counter.incrementAndGet();

        return new FlushableConsumer<T>() {
            @Override
            public void accept(T data) {
                next.accept(data);
            }

            @Override
            public void flush() {
                if (counter.decrementAndGet() == 0) {
                    next.flush();
                }
            }
        };
    }

}
