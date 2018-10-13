package org.oddjob.beanbus.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

class Internal {

    interface Onwards<P, T> extends Pipe<P> {

        void addNext(Consumer<? super T> next);

    }

    abstract static class OnwardsBase<P, T> implements Onwards<P, T> {

        abstract Pipe<? super P> to();

        @Override
        public void accept(P data) {
            to().accept(data);
        }

        @Override
        public void flush() {
            to().flush();
        }
    }

    protected static class MultiOnwards<P, T> extends OnwardsBase<P, T> {

        protected final Set<Consumer<? super T>> nexts = new LinkedHashSet<>();

        private final Pipe<? super P> to;

        public void addNext(Consumer<? super T> next) {
            nexts.add(next);
        }

        protected MultiOnwards(Section<? super P, T> section) {
            to = section.linkTo(data -> nexts.forEach(c -> c.accept(data)));
        }

        @Override
        Pipe<? super P> to() {
            return to;
        }
    }

    protected static class SplitOnwards<P, T> extends OnwardsBase<P, T> {

        private final Section<? super P, T> section;

        private Pipe<? super P> to;

        protected SplitOnwards(Section<? super P, T> section) {
            this.section = section;
        }

        @Override
        public void addNext(Consumer<? super T> next) {
            to = section.linkTo(next);
        }

        @Override
        Pipe<? super P> to() {
            return to;
        }
    }

}
