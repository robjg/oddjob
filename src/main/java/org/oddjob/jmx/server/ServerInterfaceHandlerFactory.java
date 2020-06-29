/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.jmx.client.ClientHandlerResolver;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

/**
 * Information for an interface so that it may be exposed via
 * an OddjobMBean.
 * 
 * @author Rob Gordon.
 *
 * @param <S> The class of the interface (or Object) being exposed remotely.
 * @param <T> The class of thing that will provide the signature of 
 * the handler.
 */
public interface ServerInterfaceHandlerFactory<S, T> {

	/**
	 * Provide the interface class this is the information
	 * about.
	 * 
	 * @return The class.
	 */
	Class<S> interfaceClass();
	
	/**
	 * Get the MBeanAttributeInfo for the interface.
	 *  
	 * @return An MBeanAttributeInfo array.
	 */
	MBeanAttributeInfo[] getMBeanAttributeInfo();
	
	/**
	 * Get the MBeanOperationInfo for the interface.
	 * 
	 * @return An MBeanOperationInfo array.
	 */
	MBeanOperationInfo[] getMBeanOperationInfo();
	
	/**
	 * Get the MBeanNotificationInfo for the interface.
	 * 
	 * @return An MBeanNotificationInfo array.
	 */
	MBeanNotificationInfo[] getMBeanNotificationInfo();

	
	/**
	 * Create a handler that handles communication on behalf of the
	 * MBean with the interface.
	 *  
	 * @param target The target object implementing the interface.
	 * @param toolkit The OddjobMBean.
	 * 
	 * @return An InterfaceHandler.
	 */
	ServerInterfaceHandler createServerHandler(S target, ServerSideToolkit toolkit);
	
	/**
	 * Provide the corresponding {@link org.oddjob.jmx.client.ClientInterfaceHandlerFactory}
	 * resolver.
	 * 
	 * @return The resolver.
	 */
	ClientHandlerResolver<T> clientHandlerFactory();
}
