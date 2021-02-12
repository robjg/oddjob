package org.oddjob.jmx.handlers;

import org.oddjob.arooa.parsing.DragPoint;

import java.io.Serializable;

/**
 * Info that goes across the wire for a {@link DragPoint}
 *
 * @see ConfigPointHandlerFactory
 */
class DragPointInfo implements Serializable {
    private static final long serialVersionUID = 2009020400L;

    final boolean supportsCut;

    final boolean supportsPaste;

    DragPointInfo(DragPoint serverDragPoint) {
        this.supportsCut = serverDragPoint.supportsCut();
        this.supportsPaste = serverDragPoint.supportsPaste();
    }
}
