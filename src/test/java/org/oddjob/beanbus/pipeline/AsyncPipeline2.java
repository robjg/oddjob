package org.oddjob.beanbus.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AsyncPipeline2<F> implements Pipeline<F> {

    private volatile Link<F, F> root = new RootStage<>();

    private final Executor executor;

    public AsyncPipeline2(Executor executor) {
        this.executor = executor;
    }

    public static <S> AsyncPipeline2<S> start(Executor executor) {
        return new AsyncPipeline2<>(executor);
    }

    @Override
    public <U> Stage<F, U> to(Section<? super F, U> section) {
        return root.to(section);
    }

    @Override
    public <U> Stage<F, U> to(Section<? super F, U> section, Options options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Join<F, T> join() {
        return new AsyncJoin<>();
    }

    protected interface Dispatch<P> extends Consumer<P> {

        CompletableFuture<?> complete();
    }

    protected interface Previous<I, P> {

        Dispatch<I> linkForward(Dispatch<? super P> next);
    }

    protected static class RootPipe<I> implements Dispatch<I> {

        protected final Set<Dispatch<? super I>> nexts = new LinkedHashSet<>();

        protected void addNext(Dispatch<? super I> next) {
            nexts.add(next);
        }

        @Override
        public void accept(I data) {
            nexts.forEach(c -> c.accept(data));
        }

        @Override
        public CompletableFuture<?> complete() {
            return CompletableFuture.allOf(
                    nexts.stream().map(next -> next.complete()).toArray(CompletableFuture[]::new));
        }
    }

    protected class AsyncDispatch<P, T> implements Dispatch<P> {

        private final Set<Pipe<? super P>> tos = new LinkedHashSet<>();

        private final Set<Dispatch<? super T>> nexts = new LinkedHashSet<>();

        private final PhasedWork work = new PhasedWork(
                () -> tos.forEach(Pipe::flush),
                executor);

        void addToAndNext(Pipe<? super P> to, Dispatch<? super T> next) {
            tos.add(to);
            nexts.add(next);
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

    protected class RootStage<I> implements Link<I, I>, Previous<I, I> {

        private final RootPipe<I> rootPipe = new RootPipe<>();

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section) {
            return new AsyncStage<>(this, section);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section, Options options) {
            throw new UnsupportedOperationException();
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

        private final AsyncDispatch<P, T> dispatch = new AsyncDispatch<>();

        public AsyncStage(Previous<I, P> previous, Section<? super P, T> section) {
            this.previous = previous;
            this.section = section;
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
            return new AsyncStage<>(this, section);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            throw new UnsupportedOperationException();
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

        protected Dispatch<I> maybeInitialise(Set<Stage<I, T>> joins ) {

            if (previous != null) {
                return previous;
            }

            count.set(joins.size());

            for (Stage<I, T> join : joins) {

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

        private final Set<Stage<I, T>> joins = new LinkedHashSet<>();

        private final JoinDispatch<I, T> dispatch = new JoinDispatch<>();

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {

            dispatch.addNext(next);
            return dispatch.maybeInitialise(joins);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return new AsyncStage<>(this, section);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void join(Stage<I, T> from) {

            joins.add(from);
        }
    }

    public static class AsyncOptions implements Pipeline.Options {

        private boolean async;

        public AsyncOptions async() {
            async = true;
            return this;
        }
    }
}
