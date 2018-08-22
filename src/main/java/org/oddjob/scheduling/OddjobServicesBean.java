package org.oddjob.scheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.oddjob.OddjobExecutors;
import org.oddjob.OddjobServices;
import org.oddjob.input.InputHandler;

/**
 * Bean implementations of {@link OddjobServices}
 * 
 * @author rob
 *
 */
public class OddjobServicesBean implements OddjobServices {

	private ClassLoader classLoader;

	private OddjobExecutors oddjobExecutors;
	
	private InputHandler inputHandler;
	
	@Override
	public Object getService(String serviceName) {
		if (CLASSLOADER_SERVICE.equals(serviceName)) {
			return classLoader;
		}
		if (SCHEDULED_EXECUTOR.equals(serviceName) && 
				oddjobExecutors != null) {
			return oddjobExecutors.getScheduledExecutor();
		}
		else if (POOL_EXECUTOR.equals(serviceName) &&
				oddjobExecutors != null) {
			return oddjobExecutors.getPoolExecutor();
		}
		else if (INPUT_HANDLER.equals(serviceName)) {
			return inputHandler;
		}
		else if (ODDJOB_SERVICES.equals(serviceName)) {
			return this;
		}
		throw new IllegalArgumentException("No service " + serviceName);
	}
	
	@Override
	public String serviceNameFor(Class<?> theClass, String flavour) {
		if (theClass.isAssignableFrom(ClassLoader.class)) {
			return CLASSLOADER_SERVICE;
		}
		else if (theClass.isAssignableFrom(ExecutorService.class)) {
			return POOL_EXECUTOR;
		}
		else if (theClass.isAssignableFrom(ScheduledExecutorService.class)) {
			return SCHEDULED_EXECUTOR;
		}
		else if (theClass.isAssignableFrom(InputHandler.class)) {
			return INPUT_HANDLER;
		}
		else if (theClass.isAssignableFrom(OddjobServices.class)) {
			return ODDJOB_SERVICES;
		}
		else {
			return null;
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public OddjobExecutors getOddjobExecutors() {
		return oddjobExecutors;
	}

	public void setOddjobExecutors(OddjobExecutors oddjobExecutors) {
		this.oddjobExecutors = oddjobExecutors;
	}

	public InputHandler getInputHandler() {
		return inputHandler;
	}

	public void setInputHandler(InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
