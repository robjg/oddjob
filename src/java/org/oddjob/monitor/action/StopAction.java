package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.util.ThreadManager;

/**
 * Implement a stop action.
 * 
 * @author Rob Gordon
 */

public class StopAction extends JobAction 
implements JobStateListener {

	private static final Logger logger = Logger.getLogger(StopAction.class);
	
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
			
			if (job instanceof Stateful) {
				((Stateful) job).addJobStateListener(this);
			}
		}
		else {
			setEnabled(false);
		}
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
		if (job != null && job instanceof Stateful) {
			((Stateful) job).removeJobStateListener(this);
		}
		job = null;			
	}
		
	@Override
	protected void doAction() throws Exception {
		threadManager.run(new Runnable() {
			public void run() {
				try {
					((Stoppable) job).stop();
				} catch (FailedToStopException e) {
					logger.warn(e);
				}
			}
		}, "Stopping " + job);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.state.JobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
	 */	
	public void jobStateChange(JobStateEvent event) {
		if (event.getJobState() == JobState.EXECUTING
				&& event.getSource() instanceof Stoppable) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}
