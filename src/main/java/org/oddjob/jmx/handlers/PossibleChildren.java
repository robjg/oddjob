package org.oddjob.jmx.handlers;

import org.oddjob.arooa.parsing.DragPoint;

import java.io.Serializable;
import java.util.Map;

/**
 * Info that goes across the wire for {@link DragPoint#possibleChildren()}
 *
 * @see ConfigPointHandlerFactory
 */
class PossibleChildren implements Serializable {
    private static final long serialVersionUID = 2020121800L;

    final Map<String, String> prefixMapping;

    final String[] tags;

    PossibleChildren(Map<String, String> prefixMapping, String[] tags) {
        this.prefixMapping = prefixMapping;
        this.tags = tags;
    }
}
