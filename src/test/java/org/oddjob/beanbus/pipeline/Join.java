package org.oddjob.beanbus.pipeline;

/**
 * Supports joining sections of a pipeline that have previously been split.
 *
 * @param <I> The initial type of the pipeline
 * @param <T> The type of the join. Both in and out.
 *
 * @see Pipeline#join()
 */
public interface Join<I, T> extends Connector<I, T> {

    /**
     * Add something that is a connector to this join.
     *
     * @param from The connector.
     */
    void join(Connector<I, T> from);

}
