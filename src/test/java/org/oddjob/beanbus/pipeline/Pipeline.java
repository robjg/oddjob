package org.oddjob.beanbus.pipeline;

/**
 * Define operations to create a pipeline for processing data, possibly in parallel. A pipeline is a number
 * of joined {@link Pipe}s where a flush is guaranteed to complete orderly along the pipeline ensuring no
 * work is left undone.
 *
 * @param <I> The initial type of data entering the pipeline.
 */
public interface Pipeline<I> extends Connector<I, I> {

    /**
     * Provide a point that allows previously split stages of the pipeline to join together again.
     *
     * @param <T> The type of data coming into and leaving the join.
     * @return A join point. Never null.
     */
    <T> Join<I, T> join();

    /**
     * An segment of the pipeline that supports completion operations.
     *
     * @param <I> The incoming type.
     * @param <T> The outgoing type.
     */
    interface Stage<I, T> extends Connector<I, T> {

        /**
         * Provide the pipeline as a section, that can be used with some externaly consumer
         * or could potentially be used in another pipeline.
         *
         * @return A section. Never null.
         */
        Section<I, T> asSection();

        /**
         * Create a processor from the pipeline that is able to accept data and then produce a
         * result. The result will be the last data item written by the last stage in the pipeline.
         *
         * @return A processor. Never null.
         */
        Processor<I, T> create();
    }

    /**
     * Options for use in creating sections in the pipeline. Different pipelines will support different options.
     */
    interface Options {

        /**
         * Name a stage in a pipeline.
         *
         * @param name The name. Mustn't be null.
         *
         * @return The options.
         */
        Options named(String name);
    }

}
