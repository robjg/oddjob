package org.oddjob.jmx.client;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;

/**
 * Provide tools to {@link ClientInterfaceHandlerFactory} to allow
 * the handler of method invocations to do its job.
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
	 * @param remoteOperation The Remote Operation.
	 * @param args The arguments to pass. When called from an Invocation Handler this could be
	 *                null instead of an empty array.
	 * 
	 * @return The result of the remote operation.
	 * 
	 * @throws RemoteException If something goes wrong.
	 */
	<T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
	throws RemoteException;
	
	/**
	 * Add a NotificationListener.
	 * 
	 * @param eventType The Event Type
	 * @param notificationListener The Listener.
	 */
	<T> void registerNotificationListener(NotificationType<T> eventType,
										  NotificationListener<T> notificationListener)
	throws RemoteException;
	
	/**
	 * Remove a NotificationListener.
	 * 
	 * @param eventType The Event Type
	 * @param notificationListener The Listener.
	 */
	<T> void removeNotificationListener(NotificationType<T> eventType,
			NotificationListener<T> notificationListener)
	throws RemoteException;
	
}

