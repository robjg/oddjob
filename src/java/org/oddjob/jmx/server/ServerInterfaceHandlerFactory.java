/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

import org.oddjob.jmx.client.ClientHandlerResolver;

/**
 * Information for an interface so that it may be exposed via
 * an OddjobMBean.
 * 
 * @author Rob Gordon.
 *
 */
public interface ServerInterfaceHandlerFactory<S, T> {

	/**
	 * Provide the interface class this is the information
	 * about.
	 * 
	 * @return The class.
	 */
	public Class<S> interfaceClass();
	
	/**
	 * Get the MBeanAttributeInfo for the interface.
	 *  
	 * @return An MBeanAttributeInfo array.
	 */
	public MBeanAttributeInfo[] getMBeanAttributeInfo();
	
	/**
	 * Get the MBeanOperationInfo for the interface.
	 * 
	 * @return An MBeanOperationInfo array.
	 */
	public MBeanOperationInfo[] getMBeanOperationInfo();
	
	/**
	 * Get the MBeanNotificationInfo for the interface.
	 * 
	 * @return An MBeanNotificationInfo array.
	 */
	public MBeanNotificationInfo[] getMBeanNotificationInfo();

	
	/**
	 * Create a handler that handles communication on behalf of the
	 * MBean with the interface.
	 *  
	 * @param target The target object implementing the interface.
	 * @param toolkit The OddjobMBean.
	 * 
	 * @return An InterfaceHandler.
	 */
	public ServerInterfaceHandler createServerHandler(S target, ServerSideToolkit toolkit);
	
	/**
	 * Provide the corresponding {@link org.oddjob.jmx.client.ClientInterfaceHandlerFactory}
	 * resolver.
	 * 
	 * @return The resolver.
	 */
	public ClientHandlerResolver clientHandlerFactory();
}
