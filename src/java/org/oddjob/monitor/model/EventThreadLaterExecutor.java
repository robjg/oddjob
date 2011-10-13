package org.oddjob.monitor.model;

import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

/**
 * Executor that dispatches to the Event Queue.
 * 
 * @author rob
 *
 */
public class EventThreadLaterExecutor implements Executor {

	@Override
	public void execute(final Runnable command) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				command.run();
			}
		});
	}	
}
