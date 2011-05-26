package org.oddjob.jmx.client;

import javax.management.NotificationListener;

import org.oddjob.jmx.RemoteOperation;

/**
 * Provide tools to {@link ClientInterfaceHandlerFactory} to allow
 * the handler of method invocations to do it's job.
 * 
 * @author rob
 *
 */
public interface ClientSideToolkit {

	/**
	 * Get the client session.
	 * 
	 * @return
	 */
	public ClientSession getClientSession();
	
	/**
	 * Invoke a remote operation.
	 * 
	 * @param <T> The return type of the operation.
	 * @param remoteOperation
	 * @param args
	 * 
	 * @return
	 * 
	 * @throws Throwable
	 */
	public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
	throws Throwable;
	
	/**
	 * Add a NotificationListener.
	 * 
	 * @param eventType
	 * @param notificationListener
	 */
	public void registerNotificationListener(String eventType, 
			NotificationListener notificationListener);
	
	/**
	 * Remove a NotificationListener.
	 * 
	 * @param eventType
	 * @param notificationListener
	 */
	public void removeNotificationListener(String eventType, 
			NotificationListener notificationListener);
	
}

