package org.oddjob.jobs.tasks;

import org.oddjob.input.InputRequest;

/**
 * Abstraction for parameterised execution of Task.
 * 
 * @author rob
 *
 */
public interface TaskExecutor {

	/**
	 * Provide the Parameter meta info for the task.
	 * 
	 * @return
	 */
	InputRequest[] getParameterInfo();
	
	/**
	 * Execute a task.
	 * 
	 * @param task
	 * @return
	 * @throws TaskException
	 */
	TaskView execute(Task task)
	throws TaskException;
}
