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
public class ComponentBoundry {
	
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

}