package org.oddjob.monitor.model;

import javax.swing.SwingUtilities;

/**
 * A {@link TreeEventDispatcher} that checks all events are being
 * dispatched on the Event Dispatch Thread.
 * 
 * @author rob
 *
 */
public class EventThreadOnlyDispatcher extends BaseTreeEventDispatcher {

	@Override
	protected void dispatch(Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Attempt to modify tree from not the dispatch thread.");
		}
		runnable.run();
	}
}
