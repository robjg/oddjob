package org.oddjob.beanbus.pipeline;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 *  Provides methods to create a pipeline of {@link Pipe} components.
 *  <p>
 *  It is up to client code how the pipeline is constructed but components created using this class provide the
 *  following advantages.
 *  <ul>
 *      <li>Access to the {@link Pipe#accept(Object)} method of the {@link Pipe} provided
 *      by the {@link #openWith(Pipe)} will block if a component further down the pipeline
 *      dictates that there is too much work for it.</li>
 *      <li>The {@link Pipe#accept(Object)} method will be executed asynchronously on the provided
 *      Exectutor.</li>
 *      <li>A call to {@link Pipe#flush()} will block until all work created by
 *      {@link Pipe#accept(Object)} has completed.</li>
 *  </ul>
 *
 * @param <T> The type data a the start of the pipeline.
 */
public class AsyncPipeline<T> {

    private final Blocker block;

    private final Executor executor;

    private final AtomicReference<RuntimeException> pipelineException = new AtomicReference<>();

    private final long timeout;

    /**
     * Create a pipeline with the given {@link Executor}.
     *
     * @param executor
     */
    public AsyncPipeline(Executor executor) {
        this(executor, 0);
    }

    public AsyncPipeline(Executor executor, long timeout) {
        this.executor = executor;
        this.timeout = timeout;
        block = new Blocker(timeout);
    }

    /**
     * Decorate an {@link Pipe} to be the start of a pipeline.
     *
     * @param start
     *
     * @return
     */
    public Pipe<T> openWith(Pipe<? super T> start) {

        return new Pipe<T>() {

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

    /**
     * Decorate the provided {@link Pipe} to provide one that performs an asynchronous processing of
     * data accepted via {@link Pipe#accept(Object)} which will synchronise with the
     * {@link Pipe#flush()} method.
     *
     * @param delegate
     * @param <X>
     *
     * @return
     */
    <X> Pipe<X> createSection(Pipe<? super X> delegate) {

        return new InternalSection<>(delegate, Integer.MAX_VALUE);
    }

    /**
     * Decorate the provided {@link Pipe} as per the {@link #createSection(Pipe)} method
     * but additionally, the created component will block the start of the pipeline if the work in progress is
     * greater than that specified by the {@code maxWork} parameter.
     *
     * @param delegate
     * @param maxWork
     * @param <X>
     * @return
     */
    <X> Pipe<X> createBlockSection(Pipe<? super X> delegate, int maxWork) {

        return new InternalSection<>(delegate, maxWork);
    }


    /**
     * Internal
     *
     * @param <T>
     */
    class InternalSection<T> implements Pipe<T> {

        private final Pipe<? super T> delegate;

        private final Blocker work = new Blocker(timeout);

        private final int maxWork;

        InternalSection(Pipe<? super T> delegate, int maxWork) {
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

    /**
     * A locking object that can be locked by any thread and release by any other.
     */
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
