package org.oddjob.monitor.action;

import org.oddjob.Stateful;
import org.oddjob.input.InputHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Properties;

/**
 * An action that calls a parameterised execution.
 * 
 * @author Rob Gordon 
 */
public class TaskAction extends JobAction 
implements StateListener, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(TaskAction.class);
	
	/** The job */
	private Object job = null;
	
	/** The ThreadManager that will run the job. */
	private ThreadManager threadManager;

	private AutoCloseable closeable;

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

		InputHandler.Session inputSession = new SwingInputHandler(null).start();
		this.closeable = inputSession;

		final Properties props = inputSession.handleInput(requestsArray);

		this.closeable = null;

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

	@Override
	public void close() throws Exception {
		AutoCloseable closeable = this.closeable;
		if (closeable != null) {
			closeable.close();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.state.JobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
	 */	
	public void jobStateChange(StateEvent event) {
        setEnabled(StateConditions.STARTED.test(event.getState()));
	}

}
