package org.oddjob.jobs.tasks;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.oddjob.FailedToStopException;
import org.oddjob.OddjobException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SerializableJob;
import org.oddjob.state.State;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description This job requests a task be performed
 * with optional properties.
 * 
 * 
 * @oddjob.example
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/job/TaskRequestExample.xml} 
 * 
 * @author Rob              
 */

public class TaskRequest extends SerializableJob 
implements Stoppable {
    private static final long serialVersionUID = 2015050500L;

	/** 
	 * @oddjob.property
	 * @oddjob.description The job to start
	 * @oddjob.required Yes.
	 */
	private transient TaskExecutor taskExecutor;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Properties to execute the task with.
	 * @oddjob.required No.
	 */
	private Properties properties;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Wait for the target job to finish executing. 
	 * @oddjob.required No, defaults to false.
	 */
	private volatile boolean join = true;
		
	private volatile Object response;
	
	private volatile transient Thread thread;
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The job.
	 */
	@ArooaAttribute
	synchronized public void setTaskExecutor(TaskExecutor node) {
		this.taskExecutor = node;
	}
	
	/**
	 * Get the job.
	 * 
	 * @return The node.
	 */
	synchronized public TaskExecutor getTaskExecutor() {
		return this.taskExecutor;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		if (taskExecutor == null) {
			throw new OddjobConfigException("A job to start must be provided.");
		}		
		
		TaskView taskView = taskExecutor.execute(new BasicTask(properties));
		
		if (join) {
			final CountDownLatch countDown = new CountDownLatch(1);
			
			taskView.addStateListener(new StateListener() {
				
				@Override
				public void jobStateChange(StateEvent event) {
					logger().debug("Received State [" + event.getState() + 
							"]");
					
					if (StateConditions.FINISHED.test(event.getState())) {
						countDown.countDown();
					}
				}
			});
			
			thread = Thread.currentThread();
			countDown.await();
			thread = null;
		}
		
		response = taskView.getTaskResponse();
		
		logger().debug("Set resonse to [" + response + "]");
		
		State state = taskView.lastStateEvent().getState();
		
		if (state.isException()) {
			throw new OddjobException("Exception in Task.", 
					taskView.lastStateEvent().getException());
		}
		if (state.isIncomplete()) {
			return 1;
		}
		return 0;
	}

	@Override
	protected void onStop() throws FailedToStopException {
		super.onStop();
		
		Thread thread = this.thread;
		if (thread != null) {
			thread.interrupt();
		}
	}
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Object getResponse() {
		return response;
	}
}
