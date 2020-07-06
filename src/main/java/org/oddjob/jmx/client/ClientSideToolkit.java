package org.oddjob.jmx.client;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;

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
	ClientSession getClientSession();
	
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
	<T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
	throws Throwable;
	
	/**
	 * Add a NotificationListener.
	 * 
	 * @param eventType
	 * @param notificationListener
	 */
	<T> void registerNotificationListener(NotificationType<T> eventType,
										  NotificationListener<T> notificationListener);
	
	/**
	 * Remove a NotificationListener.
	 * 
	 * @param eventType
	 * @param notificationListener
	 */
	<T> void removeNotificationListener(NotificationType<T> eventType,
			NotificationListener<T> notificationListener);
	
}

