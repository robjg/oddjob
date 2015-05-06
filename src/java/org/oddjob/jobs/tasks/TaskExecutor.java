package org.oddjob.jobs.tasks;

import java.util.Properties;

import org.oddjob.Stateful;
import org.oddjob.input.InputRequest;


public interface TaskExecutor {

	public InputRequest[] getParameterInfo();
	
	public long execute(Properties properties)
	throws TaskException;
	
	public Stateful getTaskExecution(long executionId);
}
