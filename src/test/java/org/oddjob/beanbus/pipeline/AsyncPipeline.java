package org.oddjob.beanbus.pipeline;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AsyncPipeline<T> {

    private final Blocker block;

    private final Executor executor;

    private final AtomicReference<RuntimeException> pipelineException = new AtomicReference<>();

    private final long timeout;

    public AsyncPipeline(Executor executor) {
        this(executor, 0);
    }

    public AsyncPipeline(Executor executor, long timeout) {
        this.executor = executor;
        this.timeout = timeout;
        block = new Blocker(timeout);
    }


    public FlushableConsumer<T> openWith(FlushableConsumer<T> start) {

        return new FlushableConsumer<T>() {

            @Override
            public void accept(T data) {
                Optional.ofNullable(pipelineException.get()).ifPresent( e -> { throw e; });
                block.await();
                start.accept(data);
            }

            @Override
            public void flush() {
                Optional.ofNullable(pipelineException.get()).ifPresent( e -> { throw e; });
                start.flush();
            }
        };
    }

    <X> FlushableConsumer<X> createSection(FlushableConsumer<X> delegate) {

        return new InternalSection<>(delegate, Integer.MAX_VALUE);
    }

    <X> FlushableConsumer<X> createBlockSection(FlushableConsumer<X> delegate, int maxWork) {

        return new InternalSection<>(delegate, maxWork);
    }


    class InternalSection<T> implements FlushableConsumer<T> {

        private final FlushableConsumer<T> delegate;

        private final Blocker work = new Blocker(timeout);

        private final int maxWork;

        InternalSection(FlushableConsumer<T> delegate, int maxWork) {
            this.delegate = delegate;
            this.maxWork = maxWork;
        }

        @Override
        public void accept(T data) {
            Runnable job;
            if (work.size() >= maxWork) {
                Runnable unblock = block.block();
                job = () -> {
                    try {
                        delegate.accept(data);
                    }
                    finally {
                        unblock.run();
                    }
                };
            }
            else {
                job = () -> delegate.accept(data);
            }

            Runnable unlock = work.block();
            CompletableFuture.runAsync(() -> {
                try {
                    job.run();
                }
                catch (RuntimeException e) {
                    pipelineException.set(e);
                }
                finally {
                    unlock.run();
                }
            }, executor);
        }

        @Override
        public void flush() {
            work.await();
            delegate.flush();
        }
    }

    static class Blocker {

        private final Set<CountDownLatch> blockers = ConcurrentHashMap.newKeySet();

        private final long timeout;

        Blocker() {
            this(0);
        }

        Blocker(long timeout) {
            this.timeout = timeout;
        }

        Runnable block() {

            CountDownLatch latch = new CountDownLatch(1);
            blockers.add(latch);
            return () -> {
                blockers.remove(latch);
                latch.countDown();
            };
        }

        void await() {

            blockers.forEach( latch -> {
                try {
                    if (timeout == 0) {
                        latch.await();
                    }
                    else {
                        if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                            throw new IllegalStateException("TimeOut");
                        };
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        int size() {
            return blockers.size();
        }
    }
}
