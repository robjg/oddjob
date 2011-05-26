package org.oddjob;

import org.oddjob.arooa.registry.Services;
import org.oddjob.input.InputHandler;

/**
 * Defines the internal services Oddjob uses to automatically
 * configure Jobs.
 * 
 * @author rob
 *
 */
public interface OddjobServices extends Services {

	public static final String ODDJOB_SERVICES = "oddjob-services";
	
	public static final String CLASSLOADER_SERVICE = "classloader-service";
	
	public static final String SCHEDULED_EXECUTOR = "scheduled-executor";
	
	public static final String POOL_EXECUTOR = "pool-executor";
	
	public static final String INPUT_HANDLER = "input-handler";
	
	public ClassLoader getClassLoader();
	
	public OddjobExecutors getOddjobExecutors();
	
	public InputHandler getInputHandler();
}
