package org.oddjob.monitor.action;

import java.util.Properties;

import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Stateful;
import org.oddjob.input.InputRequest;
import org.oddjob.jobs.tasks.BasicTask;
import org.oddjob.jobs.tasks.TaskException;
import org.oddjob.jobs.tasks.TaskExecutor;
import org.oddjob.monitor.Standards;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.JobAction;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.swing.SwingInputHandler;
import org.oddjob.util.ThreadManager;

/**
 * An action that calls a parameterised execution.
 * 
 * @author Rob Gordon 
 */
public class TaskAction extends JobAction 
implements StateListener {

	private static final Logger logger = LoggerFactory.getLogger(TaskAction.class);
	
	/** The job */
	private Object job = null;
	
	/** The ThreadManager that will run the job. */
	private ThreadManager threadManager;
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.monitor.model.JobOption#getName()
	 */
	public String getName() {
		return "Execute";
	}

	public String getGroup() {
		return JOB_GROUP;
	}
	
	public Integer getMnemonicKey() {
		return Standards.EXECUTE_MNEMONIC_KEY;
	}
	
	public KeyStroke getAcceleratorKey() {
		return Standards.EXECUTE_ACCELERATOR_KEY;
	}
	
	
	@Override
	protected void doPrepare(ExplorerContext explorerContext) {

		if (isPrepared()) {
			return;
		}
		
		Object component = explorerContext.getThisComponent();

		if (component instanceof TaskExecutor) {
			
			this.job = component;
			
			this.threadManager = explorerContext.getThreadManager();
				
			if (job instanceof Stateful) {
				((Stateful) job).addStateListener(this);
			}
			else {
				setEnabled(true);
			}
			setVisible(true);
		}
		else {
			this.job = null;
			setEnabled(false);
			setVisible(false);
		}
	}
	
	@Override
	protected void doFree(ExplorerContext explorerContext) {
		if (job != null && job instanceof Stateful) {
			((Stateful) job).removeStateListener(this);
		}
		job = null;
	}

	@Override
	protected void doAction() throws Exception {
		final TaskExecutor execution = (TaskExecutor) job;
		
		InputRequest[] requestsArray = execution.getParameterInfo();
		
		final Properties props = new SwingInputHandler(null).handleInput(requestsArray);
		
		if (props != null) {
			threadManager.run(new Runnable() {
				public void run() {
					try {
						execution.execute(new BasicTask(props));
					} catch (TaskException e) {
						logger.warn("Failed to execute task.", e);
					}
				}
			}, "Executing " + job);
		}	
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.state.JobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
	 */	
	public void jobStateChange(StateEvent event) {
		if (StateConditions.STARTED.test(event.getState())) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}
