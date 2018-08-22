package org.oddjob.monitor.action;

import javax.swing.KeyStroke;

import org.oddjob.Loadable;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.util.ThreadManager;

/**
 * An action that loads {@link Loadable}s.
 * 
 * @author Rob Gordon 
 */
public class LoadAction extends JobAction {

	/** The job */
	private Loadable job = null;
	
	/** The ThreadManager that will run the job. */
	private ThreadManager threadManager;
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Load";
	}

	public String getGroup() {
		return JOB_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.LOAD_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.LOAD_ACCELERATOR_KEY;
	}

	@Override
	protected void doPrepare(ExplorerContext explorerContext) {
		
		Object component = explorerContext.getThisComponent(); 
		if (component instanceof Loadable) {
			
			this.job = (Loadable) component;
			this.threadManager = explorerContext.getThreadManager();
			
			setEnabled(this.job.isLoadable());
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
				job.load();
			}
		};
		threadManager.run(runnable, "Loading " + job);
	}
}
