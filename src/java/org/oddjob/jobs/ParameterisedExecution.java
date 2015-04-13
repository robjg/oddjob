package org.oddjob.jobs;

import java.util.Properties;

import org.oddjob.input.InputRequest;


public interface ParameterisedExecution {

	public InputRequest[] getParameterInfo();
	
	public void runWith(Properties properties);
	
}
