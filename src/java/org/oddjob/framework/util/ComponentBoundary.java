/*
 * Based heavily on the Log4j NDC published under the terms of the Apache Software License
 * version 1.1 
 */
package org.oddjob.framework.util;

import org.oddjob.logging.OddjobNDC;
import org.oddjob.util.Restore;

/**
 * Handles the crossover between components. This should probably be done with
 * AOP but for now well do it long hand.
 * <p>
 * The typical usage is:
 * <pre>
 * public void someMethod() {
 *     ComponentBoundry.push(loggerName, this);
 *     try {
 *         ...
 *     }
 *     finally {
 *         ComponentBoundry.pop();
 *     }
 * }
 * </pre>
 * 
 * @author rob
 */
public class ComponentBoundary {
	
	private final String loggerName; 
	
	private final Object component;
	
	private ComponentBoundary(String loggerName, Object component) {
		this.loggerName = loggerName;
		this.component = component;
	}

	public static ComponentBoundary of(String loggerName, Object component) {
		return new ComponentBoundary(loggerName, component);
	}
	
	/**
	 * Called on entering a component method.
	 * 
	 * @param loggerName
	 *            The new diagnostic context information.
	 * @param component
	 * 			  The component.
	 */
	public static Restore push(String loggerName, Object component) {
		Restore ndcRestore = OddjobNDC.push(loggerName, component);
		Restore classLoaderRestore = ContextClassloaders.push(component);
		return new Restore() {
			
			@Override
			public void close() {
				classLoaderRestore.close();
				ndcRestore.close();
			}
		};
	}
	
	/**
	 * Wrap a unit of execution in this Component Boundary.
	 * 
	 * @param runnable The unit of execution.
	 * @return A new unit of execution that will execute within this Component Boundary.
	 */
	public Runnable wrap(Runnable runnable) {
		
		return () -> {
			try (Restore restore = push(loggerName, component)) {
				runnable.run();
			}
		};
	}
	
	/**
	 * Execute a unit of execution within this Component Boundary.
	 * 
	 * @param runnable The unit of execution.
	 */
	public void execute(Runnable runnable) {
		
		wrap(runnable).run();
	}

}