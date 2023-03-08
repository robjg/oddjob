package org.oddjob.beanbus.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *  A pipeline that supports asynchronous execution of the stages in the pipeline.
 *  <p/>
 *  To create an asynchronous stage a section must be added with {@link AsyncOptions#async()}. The pipeline
 *  guarantees that all work in a stage has completed before {@code flush} is called on the pipe.
 *  <p/>
 *  A stage can also block the start of a pipeline if its work exceeds that specified with
 *  {@link AsyncOptions#maxWork(int)}. If this option is used then the same executor should not be used for
 *  the pipeline and the process feeding it otherwise all threads could be blocked writing to the pipe with
 *  none being left to do pipeline work.
 *
 *
 * @param <F> The type data a the start of the pipeline.
 */
public class AsyncPipeline<F> implements Pipeline<F> {

    private final RootStage<F> root = new RootStage<>();

    private final Executor executor;

    private final MultiBlockGate gate = new MultiBlockGate();

    private AsyncPipeline(Executor executor) {
        this.executor = executor;
    }

    /**
     * Begin building an asynchronous pipeline.
     *
     * @param executor The executor to use for performing asynchronous work.
     * @param <S> The start type of pipeline.
     *
     * @return A pipeline supporting asynchronous processing.
     */
    public static <S> AsyncPipeline<S> begin(Executor executor) {
        return new AsyncPipeline<>(executor);
    }

    public static AsyncOptions options() {
        return new AsyncOptions();
    }

    @Override
    public <U> Stage<F, U> to(Section<? super F, U> section) {
        return root.to(section);
    }

    @Override
    public <U> Stage<F, U> to(Section<? super F, U> section, Options options) {
        return root.to(section, options);
    }

    @Override
    public <T> Join<F, T> join() {
        return new AsyncJoin<>();
    }

    public boolean isBlocked() {
        return gate.getBlockers() > 0;
    }

    protected interface Dispatch<P> extends Consumer<P> {

        CompletableFuture<?> complete();
    }

    protected interface Previous<I, P> {

        Dispatch<I> linkForward(Dispatch<? super P> next);
    }

    protected class RootPipe<I> implements Dispatch<I> {

        protected final Set<Dispatch<? super I>> nexts = new LinkedHashSet<>();

        protected void addNext(Dispatch<? super I> next) {
            nexts.add(next);
        }

        private final Consumer<I> defaultConsumer = data -> nexts.forEach(c -> c.accept(data));

        private Consumer<I> consumer = defaultConsumer;

        void useBlocking() {
            consumer = data -> {
                try {
                    gate.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                defaultConsumer.accept(data);
            };
        }

        @Override
        public void accept(I data) {
            consumer.accept(data);
        }

        @Override
        public CompletableFuture<?> complete() {
            return CompletableFuture.allOf(
                    nexts.stream().map(Dispatch::complete).toArray(CompletableFuture[]::new));
        }
    }

    protected abstract static class BaseDispatch<P, T> implements Dispatch<P> {

        private final String name;

        protected final Internal.Onwards<P, T> tos;

        protected final Set<Dispatch<? super T>> nexts = new LinkedHashSet<>();

        protected BaseDispatch(Internal.Onwards<P, T> tos, String name) {
            this.tos = tos;
            this.name = name;
        }

        void addToAndNext(Dispatch<? super T> next) {
            tos.addNext(next);
            nexts.add(next);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    protected class AsyncDispatch<P, T> extends BaseDispatch<P, T> {

        private final PhasedWork work;

        protected AsyncDispatch(Internal.Onwards<P, T> tos, String name, int maxWork) {
            super(tos, name);

            Executor useExecutor;
            if (maxWork > 0) {
                root.rootPipe.useBlocking();
                useExecutor = new MaxWork(executor,
                        maxWork,
                        gate::block,
                        gate::unblock);
            }
            else {
                useExecutor = executor;
            }

            work = new PhasedWork(
                    tos::flush,
                    useExecutor);
        }

        @Override
        public void accept(P data) {
            work.execute(() -> tos.accept(data));
        }

        @Override
        public CompletableFuture<?> complete() {
            return work.complete().thenCompose(ignored -> CompletableFuture.allOf(
                    nexts.stream().map(Dispatch::complete).toArray(CompletableFuture[]::new)));
        }
    }

    protected static class SyncDispatch<P, T> extends BaseDispatch<P, T> {

        protected SyncDispatch(Internal.Onwards<P, T> tos, String name) {
            super(tos, name);
        }

        @Override
        public void accept(P data) {
            tos.accept(data);
        }

        @Override
        public CompletableFuture<?> complete() {
            tos.flush();
            return CompletableFuture.allOf(
                    nexts.stream().map(Dispatch::complete).toArray(CompletableFuture[]::new));
        }
    }

    protected class RootStage<I> implements Connector<I, I>, Previous<I, I> {

        private final RootPipe<I> rootPipe = new RootPipe<>();

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section) {
            return to(section, options());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section, Options options) {
            return new AsyncStage<>(this, section, (AsyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super I> next) {

            rootPipe.addNext(next);
            return rootPipe;
        }

    }

    class AsyncStage<I, P, T> implements Stage<I, T>, Previous<I, T> {

        private final Previous<I, P> previous;

        private final BaseDispatch<P, T> dispatch;

        public AsyncStage(Previous<I, P> previous,
                          Section<? super P, T> section,
                          AsyncOptions options) {
            this.previous = previous;
            String name = options.name == null ? section.toString(): options.name;
            Internal.Onwards<P, T> onwards = section instanceof Splitter ?
                    new Internal.SplitOnwards<>(section) :
                    new Internal.MultiOnwards<>(section);
            if (options.async) {
                this.dispatch = new AsyncDispatch<>(onwards, name, options.maxWork);
            }
            else {
                this.dispatch = new SyncDispatch<>(onwards, name);
            }
        }

        @Override
        public Section<I, T> asSection() {

            return next -> {

                Dispatch<T> onwards = new Dispatch<>() {
                    @Override
                    public void accept(T t) {
                        next.accept(t);
                    }

                    @Override
                    public CompletableFuture<?> complete() {
                        return CompletableFuture.completedFuture(null);
                    }
                };

                Dispatch<I> start = linkForward(onwards);

                return new Pipe<>() {
                    @Override
                    public void accept(I data) {
                        start.accept(data);
                    }

                    @Override
                    public void flush() {
                        CompletableFuture<?> cf = start.complete();
                        cf.join();
                    }
                };
            };
        }

        @Override
        public Processor<I, T> create() {
            final AtomicReference<T> result = new AtomicReference<>();

            Dispatch<T> pipe = new Dispatch<>() {
                @Override
                public void accept(T data) {
                    result.set(data);
                }

                @Override
                public CompletableFuture<?> complete() {
                    return CompletableFuture.completedFuture(null);
                }
            };

            Dispatch<I> start = linkForward(pipe);

            return new Processor<>() {
                @Override
                public T complete() {
                    CompletableFuture<?> cf = start.complete();
                    cf.join();
                    return result.get();
                }

                @Override
                public void accept(I data) {
                    start.accept(data);
                }
            };
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return to(section, options());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new AsyncStage<>(this, section, (AsyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {
            dispatch.addToAndNext(next);

            return previous.linkForward(dispatch);
        }
    }

    protected static class JoinDispatch<I, T> implements Dispatch<T> {

        protected final Set<Dispatch<? super T>> nexts = new LinkedHashSet<>();

        private final AtomicInteger count = new AtomicInteger();

        private Dispatch<I> previous = null;

        protected void addNext(Dispatch<? super T> next) {
            nexts.add(next);
        }

        @SuppressWarnings("unchecked")
        protected Dispatch<I> maybeInitialise(Set<Connector<I, T>> joins ) {

            if (previous != null) {
                return previous;
            }

            count.set(joins.size());

            for (Connector<I, T> join : joins) {

                Previous<I, T> prev = (Previous<I, T>) join;
                previous = prev.linkForward(this);
            }

            if (previous == null) {
                throw new IllegalStateException("Nothing being joined.");
            }

            return previous;
        }

        @Override
        public void accept(T data) {
            nexts.forEach(c -> c.accept(data));
        }

        @Override
        public CompletableFuture<?> complete() {
            if (count.decrementAndGet() == 0) {
                return CompletableFuture.allOf(
                        nexts.stream().map(Dispatch::complete).toArray(CompletableFuture[]::new));
            }
            else {
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    protected class AsyncJoin<I, T> implements Join<I, T>, Previous<I, T> {

        private final Set<Connector<I, T>> joins = new LinkedHashSet<>();

        private final JoinDispatch<I, T> dispatch = new JoinDispatch<>();

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {

            dispatch.addNext(next);
            return dispatch.maybeInitialise(joins);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return to(section, options());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new AsyncStage<>(this, section, (AsyncOptions) options);
        }

        @Override
        public void join(Connector<I, T> from) {

            joins.add(from);
        }
    }

    public static class AsyncOptions implements Pipeline.Options {

        private final String name;

        private final boolean async;

        private final int maxWork;

        AsyncOptions() {
            this(null, false, -1);
        }

        AsyncOptions(String name, boolean async, int maxWork) {
            this.name = name;
            this.async = async;
            this.maxWork = maxWork;
        }

        public AsyncOptions async() {
            return new AsyncOptions(this.name, true, this.maxWork);
        }

        @Override
        public AsyncOptions named(String name) {
            return new AsyncOptions(name, this.async, this.maxWork);
        }

        public AsyncOptions maxWork(int maxWork) {
            return new AsyncOptions(this.name, this.async, maxWork);
        }
    }
}
