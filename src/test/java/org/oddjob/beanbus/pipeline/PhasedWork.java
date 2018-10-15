package org.oddjob.beanbus.pipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  A facility for submitting work for execution and then running a task once all the
 *  work is completed.
 */
public class PhasedWork implements Executor {

    /** A big list of completable futures could cause memory issues so clean up every so often. */
    private static final int CLEANUP_EVERY = 100;

    private final AtomicInteger workCount = new AtomicInteger();

    private final Executor executor;

    private final Runnable completion;

    private final Queue<CompletableFuture<?>> queue = new ConcurrentLinkedQueue<>();

    /**
     * Create a new instance.
     *
     * @param completion The task to run when all submitted work is done.
     * @param executor The executor to use for work.
     */
    public PhasedWork(Runnable completion, Executor executor) {
        this.completion = completion;
        this.executor = executor;
    }

    @Override
    public void execute(Runnable work) {
        queue.add(CompletableFuture.runAsync(work, executor));
        if (workCount.incrementAndGet() % CLEANUP_EVERY == 0) {
            Iterator<CompletableFuture<?>> it = queue.iterator();
            while (it.hasNext()) {
                CompletableFuture<?> cf = it.next();
                if (cf.isDone()) {
                    it.remove();
                }
            }
        }
    }

    public CompletableFuture<Void> complete() {

        List<CompletableFuture<?>> copy = new ArrayList<>();
        for (CompletableFuture<?> t = queue.poll(); t != null; t = queue.poll()) {
            copy.add(t);
        }
        CompletableFuture<?> all = CompletableFuture.allOf(copy.toArray(new CompletableFuture[copy.size()]));

        return all.thenCompose(ignore -> {
            // Sections may generate more work before they complete.
            if (queue.isEmpty()){
                return CompletableFuture.runAsync(completion, executor);
            }
            else {
                return complete();
            }
        });
    }

    public AtomicInteger getWorkCount() {
        return workCount;
    }
}
