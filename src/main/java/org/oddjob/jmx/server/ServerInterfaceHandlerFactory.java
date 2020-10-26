/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.jmx.client.HandlerVersion;

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
	 * Provide the service side class this is the factory for.
	 *
	 * @return The class. Not null.
	 */
	Class<S> serverClass();

	/**
	 * Provide the client class. This is what the client proxy will implement.
	 * It will mainly be the same as the server class but sometime there is
	 * extra functionality the client needs and these classes will provide that.
	 *
	 * @return The class. Not null.
	 */
	Class<T> clientClass();

	/**
	 * Get the version of this handler.
	 *
	 * @return The version. Not null.
	 */
	HandlerVersion getHandlerVersion();

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
	
}
