package org.oddjob.beanbus.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SyncPipeline<I> implements Pipeline<I> {

    private volatile Connector<I, I> root = new RootStage<>();

    public static <P> Pipeline<P> begin() {
        return new SyncPipeline<>();
    }

    public static SyncOptions options() {
        return new SyncOptions();
    }

    @Override
    public <U> Stage<I, U> to(Section<? super I, U> section) {

        return to(section, options());
    }

    @Override
    public <U> Stage<I, U> to(Section<? super I, U> section, Options options) {
        return root.to(section, options);
    }

    @Override
    public <T> Join<I, T> join() {
        return new SyncJoin<>();
    }

    protected interface Dispatch<P> extends Consumer<P> {

        void complete();
    }

    protected interface Previous<I, P> {

        Dispatch<I> linkForward(Dispatch<? super P> next);
    }

    protected static class RootDispatch<I> implements Dispatch<I> {

        protected final Set<Dispatch<? super I>> nexts = new LinkedHashSet<>();

        protected void addNext(Dispatch<? super I>  next) {
            nexts.add(next);
        }

        @Override
        public void accept(I data) {
            nexts.forEach(c -> c.accept(data));
        }

        @Override
        public void complete() {
            nexts.forEach(Dispatch::complete);
        }
    }

    protected abstract static class DispatchBase<P, T> implements Dispatch<P> {

        private final String name;

        protected final Set<Dispatch<? super T>> nexts = new LinkedHashSet<>();

        protected DispatchBase(String name) {
            this.name = name;
        }

        abstract Pipe<? super P> to();

        protected void addNext(Dispatch<? super T> next) {
            nexts.add(next);
        }

        @Override
        public void accept(P data) {
            to().accept(data);
        }

        @Override
        public void complete() {

            to().flush();
            nexts.forEach(Dispatch::complete);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    protected static class SyncDispatch<P, T> extends DispatchBase<P, T> {

        private final Pipe<? super P> to;

        protected SyncDispatch(Section<? super P, T> section, String name) {
            super(name);
            to = section.linkTo(data -> nexts.forEach(c -> c.accept(data)));
        }

        @Override
        Pipe<? super P> to() {
            return to;
        }
    }

    protected static class SyncSplitDispatch<P, T> extends DispatchBase<P, T> {

        private final Section<? super P, T> section;

        private Pipe<? super P> to;

        protected SyncSplitDispatch(Section<? super P, T> section, String name) {
            super(name);
            this.section = section;
        }

        @Override
        protected void addNext(Dispatch<? super T> next) {
            to = section.linkTo(next);
            super.addNext(next);
        }

        @Override
        Pipe<? super P> to() {
            return to;
        }
    }

    protected static class RootStage<I> implements Connector<I, I>, Previous<I, I> {

        private final RootDispatch<I> rootDispatch = new RootDispatch<>();

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section) {
            return to(section, options());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super I, U> section, Options options) {
            return new SyncStage<>(this, section, (SyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super I> next) {
            rootDispatch.addNext(next);
            return rootDispatch;
        }
    }


    protected static class SyncStage<I, P, T> implements Stage<I, T>, Previous<I, T> {

        private final Previous<I, P> previous;

        private final DispatchBase<P, T> dispatch;

        public SyncStage(Previous<I, P> previous, Section<? super P, T> section, SyncOptions options) {
            this.previous = previous;
            String name = options.name == null ? section.toString(): options.name;
            this.dispatch = section instanceof Splitter ?
                    new SyncSplitDispatch<>(section, name) :
                    new SyncDispatch<>(section, name);
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section) {
            return to(section, options());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new SyncStage<>(this, section, (SyncOptions) options);
        }

        @Override
        public Dispatch<I> linkForward(Dispatch<? super T> next) {

            dispatch.addNext(next);

            return previous.linkForward(dispatch);
        }

        @Override
        public Section<I, T> asSection() {

            return next -> {
                Dispatch<I> start = linkForward(new Dispatch<T>() {
                    @Override
                    public void accept(T data) {
                        next.accept(data);
                    }

                    @Override
                    public void complete() {

                    }
                });

                return new Pipe<I>() {
                    @Override
                    public void accept(I data) {
                        start.accept(data);
                    }

                    @Override
                    public void flush() {
                        start.complete();
                    }
                };
            };
        }

        @Override
        public Processor<I, T> create() {

            final AtomicReference<T> result = new AtomicReference<>();

            Dispatch<T> dispatch = new Dispatch<T>() {
                @Override
                public void accept(T data) {
                    result.set(data);
                }

                @Override
                public void complete() {

                }
            };

            Dispatch<I> start = linkForward(dispatch);

            return new Processor<I, T>() {
                @Override
                public T complete() {
                    start.complete();
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

        private final Set<Connector<I, T>> joins = new LinkedHashSet<>();

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
                        next.complete();
                    }
                }
            };

            Dispatch<I> previous = null;

            for (Connector<I, T> join : joins) {

                Previous<I, T> prev = (Previous<I, T>) join;
                previous = prev.linkForward(new Dispatch<T>() {
                    @Override
                    public void accept(T data) {
                        theJoin.accept(data);
                    }

                    @Override
                    public void complete() {
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
            return to(section, options());
        }

        @Override
        public <U> Stage<I, U> to(Section<? super T, U> section, Options options) {
            return new SyncStage<>(this, section, (SyncOptions) options);
        }

        @Override
        public void join(Connector<I, T> from) {

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
