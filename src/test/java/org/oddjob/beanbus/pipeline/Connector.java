package org.oddjob.beanbus.pipeline;


/**
 * Part of a pipeline that can connect to other sections. A connector can connect to multiple sections, in
 * which case all connected sections receive a copy of the data passing through the connection.
 * <p/>
 * If a connector is connecting to the same {@link Pipe} then the pipe will only receive the data once. This
 * is how sections in {@link Splits} can channel data to different pipes.
 *
 * @param <I> The incoming type of the pipeline.
 * @param <T> The type this connector connects to.
 */
public interface Connector<I, T> {

    /**
     * Connect to a new section of a pipeline.
     *
     * @param section The section.
     * @param <U> The outgoing type of the section.
     *
     * @return An stage of a pipeline.
     */
    <U> Pipeline.Stage<I, U> to(Section<? super T, U> section);

    /**
     * Connect to a new section of a pipeline with options.
     *
     * @param section The section.
     * @param options The options to connect with.
     * @param <U> The outgoing type of the section.
     *
     * @return A stage of a pipeline.
     */
    <U> Pipeline.Stage<I ,U> to(Section<? super T, U> section, Pipeline.Options options);
}

