/*
 * Based heavily on the Log4j NDC published under the terms of the Apache Software License
 * version 1.1 
 */
package org.oddjob.framework;

import org.oddjob.logging.OddjobNDC;

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
public class ComponentBoundry {
	
	/**
	 * Called on entering a component method.
	 * 
	 * @param loggerName
	 *            The new diagnostic context information.
	 * @param component
	 * 			  The component.
	 */
	public static void push(String loggerName, Object component) {
		OddjobNDC.push(loggerName, component);
		ContextClassloaders.push(component);
	}

	/**
	 * Called on leaving a component method.
	 */
	public static void pop() {
		ContextClassloaders.pop();
		OddjobNDC.pop();
	}

}