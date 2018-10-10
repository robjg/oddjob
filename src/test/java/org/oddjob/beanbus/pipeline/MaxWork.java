package org.oddjob.beanbus.pipeline;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class MaxWork implements Executor {

    private final Executor delegate;

    private final int maxWork;

    private final Runnable block;

    private final Runnable unBlock;

    private final AtomicInteger workCount = new AtomicInteger();

    public MaxWork(Executor delegate, int maxWork, Runnable block, Runnable unBlock) {
        this.delegate = delegate;
        this.maxWork = maxWork;
        this.block = block;
        this.unBlock = unBlock;
    }

    @Override
    public void execute(Runnable command) {
        Runnable job;
        if (workCount.incrementAndGet() >= maxWork) {
            block.run();
            job = () -> {
                try {
                    command.run();
                }
                finally {
                    workCount.decrementAndGet();
                    unBlock.run();
                }
            };
        }
        else {
            job = command;
        }
        delegate.execute(job);
    }
}
