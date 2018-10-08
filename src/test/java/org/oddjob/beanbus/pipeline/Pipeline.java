package org.oddjob.beanbus.pipeline;

public interface Pipeline<I> extends Link<I, I> {

    <T> Join<I, T> join();

    interface Stage<I, T> extends Link<I, T> {

        Section<I, T> asSection();

        Processor<I, T> create();
    }

    interface Join<I, T> extends Link<I, T> {

        void join(Stage<I, T> from);

    }

    interface Options {

    }

}
