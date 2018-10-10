package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PhasedWork implements Executor {

    private final Executor executor;

    private final Runnable completion;

    private final Queue<CompletableFuture<?>> queue = new ConcurrentLinkedQueue<>();

    public PhasedWork(Runnable completion, Executor executor) {
        this.completion = completion;
        this.executor = executor;
    }

    @Override
    public void execute(Runnable work) {
        queue.add(CompletableFuture.runAsync(work, executor));
    }

    public CompletableFuture<?> complete() {

        List<CompletableFuture<?>> copy = new ArrayList<>();
        for (CompletableFuture<?> t = queue.poll(); t != null; t = queue.poll()) {
            copy.add(t);
        }
        CompletableFuture<?> all = CompletableFuture.allOf(copy.toArray(new CompletableFuture[copy.size()]));
        return all.thenRun(completion);
    }
}
