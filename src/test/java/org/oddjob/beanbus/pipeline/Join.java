package org.oddjob.beanbus.pipeline;

public interface Join<I, T> extends Link<I, T> {

    void join(Link<I, T> from);

}
