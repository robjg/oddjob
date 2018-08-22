package org.oddjob.monitor.model;

import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

/**
 * An Executor that checks all events are being dispatched on the Event Dispatch Thread.
 * 
 * @author rob
 *
 */
public class EventThreadOnlyDispatcher implements Executor {

	@Override
	public void execute(Runnable command) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Attempt to modify tree from not the dispatch thread.");
		}
		command.run();
	}
}
