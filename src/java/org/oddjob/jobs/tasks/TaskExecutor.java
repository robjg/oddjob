package org.oddjob.jobs.tasks;

import org.oddjob.input.InputRequest;


public interface TaskExecutor {

	public InputRequest[] getParameterInfo();
	
	public TaskView execute(Task task)
	throws TaskException;
}
