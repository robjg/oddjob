/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOperation;

/**
 * Handle communication between an instance of an interface and
 * an MBean.
 * <p>
 * Implementations handle invocations on the methods of an interface and
 * provide notifications for the MBean to propagate onwards.
 * <p>
 * Finally the handler must clear up any resources it might have created
 * in the execution of it duties, such as removing listeners. This is
 * done in the destroy() method.
 * <p>
 * 
 * @author Rob Gordon.
 *
 */
public interface ServerInterfaceHandler {

	/**
	 * Invoke an operation. 
	 * <p>
	 * Note that this method is not 
	 * parameterised on return type. This is because it is 
	 * called from JMX which has no interest in the return type.
	 * Adding a return type would also require that return values
	 * from the methods were cast to the parameter which would
	 * be a lot of code for no advantage.
	 * 
	 * @param operation The action (method) name.
	 * @param params The parameter object array.
	 * 
	 * @return The result if any.
	 * 
	 * @throws Throwable if anything goes wrong
	 */
	Object invoke(RemoteOperation<?> operation, final Object[] params) throws Throwable;
		
	/**
	 * Clear up any resource this handler might have created.
	 */
	void destroy();
}