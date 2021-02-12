package org.oddjob.monitor.context;

import java.util.Optional;

/**
 * A General purpose context with an Ancestor. Something that provides information (a context) to things like
 * a monitor.
 */
public interface AncestorContext {

    /**
     * Get the component this is the context for.
     *
     * @return
     */
    Object getThisComponent();

    /**
     * Get the parent context of this context.
     *
     * @return The parent, or null if this is the root.
     */
    AncestorContext getParent();

    /**
     * Find a component of a certain type in the hierarchy.
     *
     * @param type
     * @param <T>
     * @return
     */
    default <T> Optional<T> findAncestorOfType(Class<T> type) {
        Object component = this.getThisComponent();
        if (type.isInstance(component)) {
            return Optional.of(type.cast(component));
        }
        return Optional.ofNullable(getParent())
                .flatMap(p -> p.findAncestorOfType(type));
    }
}
