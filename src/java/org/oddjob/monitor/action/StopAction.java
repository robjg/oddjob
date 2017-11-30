package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.util.ThreadManager;

/**
 * Implement a stop action.
 * 
 * @author Rob Gordon
 */

public class StopAction extends JobAction {

	private static final Logger logger = LoggerFactory.getLogger(StopAction.class);
	
	/** The job. */
	private Object job = null;
	
	/** The ThreadManager that will run the stop. */
	private ThreadManager threadManager;
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Stop";
	}

	public String getGroup() {
		return JOB_GROUP;
	}

	public Integer getMnemonicKey() {
		return Standards.STOP_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.STOP_ACCELERATOR_KEY;
	}
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {

		if (isPrepared()) {
			return;
		}
		
		Object component = explorerContext.getThisComponent();
		
		if (component instanceof Stoppable) {
			this.job = component;
			setEnabled(true);
			this.threadManager = explorerContext.getThreadManager();
		}
		else {
			setEnabled(false);
		}
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
		job = null;			
	}
		
	@Override
	protected void doAction() throws Exception {
		threadManager.run(new Runnable() {
			public void run() {
				try {
					((Stoppable) job).stop();
				} catch (FailedToStopException e) {
					logger.warn("Failed to stop.", e);
				}
			}
		}, "Stopping " + job);
	}
	
}
