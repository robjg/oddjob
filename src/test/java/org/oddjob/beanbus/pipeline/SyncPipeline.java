package org.oddjob.beanbus.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SyncPipeline<I> implements Pipeline<I> {

    private volatile Link<I, I> root = new RootStage<>();

    public static <P> Pipeline<P> start() {
        return new SyncPipeline<>();
    }

    public static SyncOptions withOptions() {
        return new SyncOptions();
    }

    @Override
    public <U> Stage<I, U> to(Section<? super I, U> section) {

        return to(section, withOptions());
    }

    @Override
    public <U> Stage<I, U> to(Section<? super I, U> section, Options options) {
        return root.to(section, options);
    }

    @Override
    public <T> Join<I, T> join() {
        return new SyncJoin<>();
    }

    protected interface Dispatch<P> extends Pipe<P> {

    }

    protected interface Previous<I, P> {

        Dispatch<I> linkForward(Dispatch<? super P> next);
    }

    protected static class RootPipe<I> implements Dispatch<I> {

        protected final Set<Dispatch<? super I>> nexts = new LinkedHashSet<>();

        protected void addNext(Dispatch<? super I>  next) {
            nexts.add(next);
        }

        @Override
        public void accept(I data) {
            nexts.forEach(c -> c.accept(data));
        }

        @Override
        public void flush() {
            nexts.forEach(Dispatch::flush);
        }
    }

    protected abstract static class DispatchBase<P, T> implements Dispatch<P> {

        private final String name;

        protected final Set<Pipe<? super P>> tos = new LinkedHashSet<>();

        protected final Set<Dispatch<? super T>> nexts = new LinkedHashSet<>();

        protected DispatchBase(String name) {
            this.name = name;
        }

        protected void addToAndNext(Pipe<? super P> to, Dispatch<? super T> next) {
            tos.add(to);
            nexts.add(next);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    protected static class SyncDispatch<P, T> extends DispatchBase<P, T> {

        protected SyncDispatch(String name) {
            super(name);
        }

        @Override
        public void accept(P data) {
            tos.forEach(c -> c.accept(data));
        }

        @Override
        public void flush() {

            tos.forEach(Pipe::flush);
            nexts.forEach(Dispatch::flush);
        }
    }

    protected static class RootStage<I> implements Link<I, I>, Previous<I, I> {

        private final RootPipe<I> rootPipe = new RootPipe<>();

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section) {
            return to(section, withOptions());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section, Options options) {
            return new SyncStage<>(this, section, (SyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super I> next) {
            rootPipe.addNext(next);
            return rootPipe;
        }
    }

    protected static class SyncStage<I, P, T> implements Stage<I, T>, Previous<I, T> {

        private final Previous<I, P> previous;

        private final Section<? super P, T> section;

        private final SyncDispatch<P, T> dispatch;

        public SyncStage(Previous<I, P> previous, Section<? super P, T> section, SyncOptions options) {
            this.previous = previous;
            this.section = section;
            String name = options.name == null ? section.toString(): options.name;
            this.dispatch = new SyncDispatch<>(name);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return to(section, withOptions());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new SyncStage<>(this, section, (SyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {

            dispatch.addToAndNext(section.linkTo(next), next);

            return previous.linkForward(dispatch);
        }

        @Override
        public Section<I, T> asSection() {

            return next -> linkForward(new Dispatch<T>() {
                @Override
                public void accept(T data) {
                    next.accept(data);
                }

                @Override
                public void flush() {

                }
            });
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
                public void flush() {

                }
            };

            Pipe<I> start = linkForward(pipe);

            return new Processor<I, T>() {
                @Override
                public T complete() {
                    start.flush();
                    return result.get();
                }

                @Override
                public void accept(I data) {
                    start.accept(data);
                }
            };
        }
    }

    protected static class SyncJoin<I, T> implements Join<I, T>, Previous<I, T> {

        private final Set<Link<I, T>> joins = new LinkedHashSet<>();

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {

            Pipe<T> theJoin = new Pipe<T>() {
                AtomicInteger count = new AtomicInteger(joins.size());

                @Override
                public void accept(T data) {
                    next.accept(data);
                }

                @Override
                public void flush() {
                    if (count.decrementAndGet() == 0) {
                        next.flush();
                    }
                }
            };

            Dispatch<I> previous = null;

            for (Link<I, T> join : joins) {

                Previous<I, T> prev = (Previous<I, T>) join;
                previous = prev.linkForward(new Dispatch<T>() {
                    @Override
                    public void accept(T data) {
                        theJoin.accept(data);
                    }

                    @Override
                    public void flush() {
                        theJoin.flush();
                    }
                });
            }

            if (previous == null) {
                throw new IllegalStateException("Nothing being joined.");
            }

            return previous;
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return to(section, withOptions());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new SyncStage<>(this, section, (SyncOptions) options);
        }

        @Override
        public void join(Link<I, T> from) {

            joins.add(from);
        }
    }

    public static class SyncOptions implements Pipeline.Options {

        private final String name;

        SyncOptions() {
            this(null);
        }

        SyncOptions(String name) {
            this.name = name;
        }

        @Override
        public Options named(String name) {
            return new SyncOptions(name);
        }
    }
}
