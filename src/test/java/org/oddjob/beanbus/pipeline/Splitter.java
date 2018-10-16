package org.oddjob.beanbus.pipeline;

import java.util.function.Consumer;

/**
 * Marker interface for something that will split data.
 * <p/>
 * Normally a pipeline will handle splitting data by providing a single consumer to the section via the
 * {@link Section#linkTo(Consumer)} method, and this consumer handles passing data to all downstream sections
 * connected with {@link Pipeline#to(Section)}. If a section implements this interface a consumer per downstream
 * section will be given to the section so that it can handle the splitting strategy. If the downstream section was
 * added using the {@link Pipeline.Options#named(String)} method hen its {@code toString()} method will return that
 * name so that it can be used in splitting.
 */
public interface Splitter<T, U> extends Section<T, U> {
}
