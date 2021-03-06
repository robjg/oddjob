package org.oddjob.jmx.server;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.remote.Notification;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.util.NotifierListener;

/**
 * An InterfaceHandlersFriend is able to help an interface handler to it's 
 * job.
 * 
 * @author Rob Gordon.
 */

public interface ServerSideToolkit {

	/**
	 * Send a notification. This can be provided by
	 * BroadcastNotificationSupport in the implementation.
	 *  
	 * @param notification The notification.
	 */
	void sendNotification(Notification<?> notification);

	<T> void setNotifierListener(NotificationType<T> type,  NotifierListener<T> notifierListener);

	/**
	 * Create a Notification.
	 *
	 * @param type The notification type.
	 * @param userData Optional user data.
	 *
	 * @return A new notification.
	 */
	<T> Notification<T> createNotification(NotificationType<T> type, T userData);
			
	/**
	 * Used by handlers to execute functionality while
	 * holding the resync lock.
	 * 
	 * @param runnable The functionality to run.
	 */
	void runSynchronized(Runnable runnable);
	
	/**
	 * Gives handlers access to the server context.
	 * 
	 * @return The server context for this MBean.
	 */
	ServerContext getContext();
	
	/**
	 * Get the remote controller bean.
	 * 
	 * @return
	 */
	RemoteOddjobBean getRemoteBean();
	
	/**
	 * Get the server session.
	 * 
	 * @return
	 */
	ServerSession getServerSession();
	
}