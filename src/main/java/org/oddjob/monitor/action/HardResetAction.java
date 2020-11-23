package org.oddjob.monitor.action;

import org.oddjob.Resettable;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.util.ThreadManager;

import javax.swing.*;

/**
 * An action that performs a hard reset on a resettable.
 * 
 * @author Rob Gordon 
 */

public class HardResetAction extends JobAction {

	/** The job */
	private Object job = null;

	/** The ThreadManager that will run the reset. */
	private ThreadManager threadManager;
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Hard Reset";
	}
	
	public String getGroup() {
		return JOB_GROUP;
	}

	public Integer getMnemonicKey() {
		return Standards.HARD_RESET_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.HARD_RESET_ACCELERATOR_KEY;
	}
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		Object component = explorerContext.getThisComponent();
		
		if (!(component instanceof Resettable)) {
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
	protected void doAction() {
		threadManager.run(() -> ((Resettable) job)
				.hardReset(), "Hard Reset of " + job);
	}
	
}
