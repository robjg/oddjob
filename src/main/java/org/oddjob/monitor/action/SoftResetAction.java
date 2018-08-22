package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.oddjob.Resetable;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.util.ThreadManager;

/**
 * Perform a soft reset action.
 * 
 * @author Rob Gordon 
 */

public class SoftResetAction extends JobAction {

	/** The job. */
	private Object job = null;

	/** The ThreadManager that will run the reset. */
	private ThreadManager threadManager;

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Soft Reset";
	}

	public String getGroup() {
		return JOB_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.SOFT_RESET_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.SOFT_RESET_ACCELERATOR_KEY;
	}
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		Object component = explorerContext.getThisComponent();
		if (!(component instanceof Resetable)) {
			this.job = null;
			setEnabled(false);
		}
		else {
			this.job = component;
			setEnabled(true);
			this.threadManager = explorerContext.getThreadManager();		
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
				((Resetable)job).softReset();
			}
		}, "Soft Reset of " + job);
	}
}
