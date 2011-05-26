package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.oddjob.Stateful;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.util.ThreadManager;

/**
 * An action that executes a job.
 * 
 * @author Rob Gordon 
 */
public class ExecuteAction extends JobAction 
implements JobStateListener {

	/** The job */
	private Object job = null;
	
	/** The ThreadManager that will run the job. */
	private ThreadManager threadManager;
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Run";
	}

	public String getGroup() {
		return JOB_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.RUN_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.RUN_ACCELERATOR_KEY;
	}
	
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {

		if (isPrepared()) {
			return;
		}
		
		Object component = explorerContext.getThisComponent();

		if (!(component instanceof Runnable)) {
			this.job = null;
			setEnabled(false);
		}
		else {
			this.job = component;
				
			this.threadManager = explorerContext.getThreadManager();
				
			if (job instanceof Stateful) {
				((Stateful) job).addJobStateListener(this);
			}
			else {
				setEnabled(true);
			}
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
		threadManager.run(((Runnable) job), "Executing " + job);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.state.JobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
	 */	
	public void jobStateChange(JobStateEvent event) {
		if (event.getJobState() == JobState.READY) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}
