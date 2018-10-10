package org.oddjob.beanbus.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AsyncPipeline2<F> implements Pipeline<F> {

    private final RootStage<F> root = new RootStage<>();

    private final Executor executor;

    private final MultiBlockGate gate = new MultiBlockGate();

    public AsyncPipeline2(Executor executor) {
        this.executor = executor;
    }

    public static <S> AsyncPipeline2<S> start(Executor executor) {
        return new AsyncPipeline2<>(executor);
    }

    public static AsyncOptions withOptions() {
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
                    nexts.stream().map(next -> next.complete()).toArray(CompletableFuture[]::new));
        }
    }

    protected abstract class BaseDispatch<P, T> implements Dispatch<P> {

        private final String name;

        protected final Set<Pipe<? super P>> tos = new LinkedHashSet<>();

        protected final Set<Dispatch<? super T>> nexts = new LinkedHashSet<>();

        protected BaseDispatch(String name) {
            this.name = name;
        }

        void addToAndNext(Pipe<? super P> to, Dispatch<? super T> next) {
            tos.add(to);
            nexts.add(next);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    protected class AsyncDispatch<P, T> extends BaseDispatch<P, T> {

        private final PhasedWork work;

        protected AsyncDispatch(String name, int maxWork) {
            super(name);

            Executor useExecutor;
            if (maxWork > 0) {
                root.rootPipe.useBlocking();
                useExecutor = new MaxWork(executor,
                        maxWork,
                        () -> gate.block(),
                        () -> gate.unblock());
            }
            else {
                useExecutor = executor;
            }

            work = new PhasedWork(
                    () -> tos.forEach(Pipe::flush),
                    useExecutor);
        }

        @Override
        public void accept(P data) {
            tos.forEach(c -> work.execute(() -> c.accept(data)));
        }

        @Override
        public CompletableFuture<?> complete() {
            return work.complete().thenCompose(ignored -> CompletableFuture.allOf(
                    nexts.stream().map(next -> next.complete()).toArray(CompletableFuture[]::new)));
        }
    }

    protected class SyncDispatch<P, T> extends BaseDispatch<P, T> {

        protected SyncDispatch(String name) {
            super(name);
        }

        @Override
        public void accept(P data) {
            tos.forEach(c -> c.accept(data));
        }

        @Override
        public CompletableFuture<?> complete() {
            tos.forEach(Pipe::flush);
            return CompletableFuture.allOf(
                    nexts.stream().map(next -> next.complete()).toArray(CompletableFuture[]::new));
        }
    }

    protected class RootStage<I> implements Link<I, I>, Previous<I, I> {

        private final RootPipe<I> rootPipe = new RootPipe<>();

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section) {
            return to(section, withOptions());
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

        private final Section<? super P, T> section;

        private final BaseDispatch<P, T> dispatch;

        public AsyncStage(Previous<I, P> previous,
                          Section<? super P, T> section,
                          AsyncOptions options) {
            this.previous = previous;
            this.section = section;
            String name = options.name == null ? section.toString(): options.name;
            if (options.async) {
                this.dispatch = new AsyncDispatch<>(name, options.maxWork);
            }
            else {
                this.dispatch = new SyncDispatch<>(name);
            }
        }

        @Override
        public Section<I, T> asSection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Processor<I, T> create() {
            final AtomicReference<T> result = new AtomicReference<>();

            Dispatch<T> pipe = new Dispatch<T>() {
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

            return new Processor<I, T>() {
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
            return to(section, withOptions());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new AsyncStage<>(this, section, (AsyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {
            dispatch.addToAndNext(section.linkTo(next), next);

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

        protected Dispatch<I> maybeInitialise(Set<Link<I, T>> joins ) {

            if (previous != null) {
                return previous;
            }

            count.set(joins.size());

            for (Link<I, T> join : joins) {

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
                        nexts.stream().map(next -> next.complete()).toArray(CompletableFuture[]::new));
            }
            else {
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    protected class AsyncJoin<I, T> implements Join<I, T>, Previous<I, T> {

        private final Set<Link<I, T>> joins = new LinkedHashSet<>();

        private final JoinDispatch<I, T> dispatch = new JoinDispatch<>();

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {

            dispatch.addNext(next);
            return dispatch.maybeInitialise(joins);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return to(section, withOptions());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new AsyncStage<>(this, section, (AsyncOptions) options);
        }

        @Override
        public void join(Link<I, T> from) {

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
        public Options named(String name) {
            return new AsyncOptions(name, this.async, this.maxWork);
        }

        public AsyncOptions maxWork(int maxWork) {
            return new AsyncOptions(this.name, this.async, maxWork);
        }
    }
}
