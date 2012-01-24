package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.oddjob.Forceable;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.util.ThreadManager;

/**
 * An action that loads Forces {@link Forceable}s.
 * 
 * @author Rob Gordon 
 */
public class ForceAction extends JobAction {

	/** The job */
	private Forceable job = null;
	
	/** The ThreadManager that will run the job. */
	private ThreadManager threadManager;
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Force";
	}

	public String getGroup() {
		return JOB_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.FORCE_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return null;
	}

	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		Object component = explorerContext.getThisComponent(); 
		if (component instanceof Forceable) {
			
			this.job = (Forceable) component;
			this.threadManager = explorerContext.getThreadManager();
			
			setEnabled(true);
			setVisible(true);
		}
		else {
			setEnabled(false);
			setVisible(false);
		}
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
		
		job = null;
		threadManager = null;		
	}

	@Override
	protected void doAction() throws Exception {
		
		Runnable runnable = new Runnable() {
			public void run() {
				job.force();
			}
		};
		threadManager.run(runnable, "Forcing " + job);
	}
}
