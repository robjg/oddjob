package org.oddjob.jobs.tasks;

import java.util.Properties;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SerializableJob;
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

public class TaskRequest extends SerializableJob {
    private static final long serialVersionUID = 2015050500L;

	/** 
	 * @oddjob.property
	 * @oddjob.description The job to start
	 * @oddjob.required Yes.
	 */
	private transient TaskExecutor task;
	
	private Properties properties;
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The job.
	 */
	@ArooaAttribute
	synchronized public void setTask(TaskExecutor node) {
		this.task = node;
	}
	
	/**
	 * Get the job.
	 * 
	 * @return The node.
	 */
	synchronized public TaskExecutor getTask() {
		return this.task;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		if (task == null) {
			throw new OddjobConfigException("A job to start must be provided.");
		}		
		
		task.execute(properties);
		
		return 0;	
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}	
}
