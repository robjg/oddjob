package org.oddjob.beanbus.pipeline;

public interface Join<I, T> extends Connector<I, T> {

    void join(Connector<I, T> from);

}
