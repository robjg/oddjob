package org.oddjob.remote.things;

import org.oddjob.arooa.parsing.QTag;

import java.util.function.Consumer;

/**
 * A remote idea of a {@link org.oddjob.arooa.parsing.DragPoint}. Intended to handle the
 * remote aspects of configuring a component.
 */
public interface ConfigPoint {

    /**
     * Add a consumer that will be updated about what the config point supports.
     *
     * @param consumer A Consumer of the support information
     */
    void addConfigurationSupportsConsumer(Consumer<? super ConfigOperationInfo> consumer);

    void removeConfigurationSupportsConsumer(Consumer<? super ConfigOperationInfo> consumer);

    /**
     * Copy this Configuration Points configuration and remove it.
     *
     * @return Text XML.
     */
    String cut();

    /**
     * Provide a copy of ths Configuration Points configuration as XML.
     *
     * @return Text XML.
     */
    String copy();

    /**
     * Delete this Configuration Point.
     */
    void delete();

    /**
     * Parse an XML or JSON Text configuration and add the resultant component
     * to this Configuration Point with the given index.
     *
     * @param index The index. -1 will append.
     * @param config The configuration.
     *
     */
    void paste(int index, String config);

    /**
     * List the possible children a Drag Point can have for Add Job
     * functionality. {@link ConfigOperationInfo#isPasteSupported()} must be true for this
     * to work.
     *
     * @return All that child tags that could be added with a paste
     * operation.
     */
    QTag[] possibleChildren();

}
