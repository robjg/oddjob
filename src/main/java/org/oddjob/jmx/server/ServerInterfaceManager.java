/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.remote.Implementation;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import java.util.Set;

/**
 * An InterfaceManager collects together the interfaces to be exposed by 
 * a component. 
 *
 */
public interface ServerInterfaceManager {

	/**
	 * Return the client interfaces supported.
	 *
	 * @return The interfaces.
	 */
	Implementation<?>[] allClientInfo();

	/**
	 * Get the MBeanInfo based on all the interfaces.
	 *
	 * @return
	 */
	MBeanInfo getMBeanInfo();


	/**
	 * Get the Notification Types from all Interfaces.
	 *
	 * @return A Set of Types. Might be empty, but never null.
	 */
	Set<NotificationType<?>> getNotificationTypes();

	/**
	 * Invoke a method using the arguments as received by an MBean.
	 * 
	 * @param actionName The action (method) name.
	 * @param params An array of object that are the parameters.
	 * @param signature An array of Strings that are class names.
	 * 
	 * @return The result of the method call.
	 * 
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	Object invoke(String actionName, Object[] params, String[] signature)
	throws MBeanException, ReflectionException;

	/**
	 * Called when an MBean is being destroyed. Used to InterfaceHandlers
	 * a chance to remove listeners.
	 */
	void destroy();
	
	
}
