package org.oddjob.jmx.server;

import javax.management.Notification;

import org.oddjob.jmx.RemoteOddjobBean;

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
	public void sendNotification(Notification notification);
	
	public Notification createNotification(String type);
			
	/**
	 * Used by handlers to execute functionality while
	 * holding the resync lock.
	 * 
	 * @param runnable The functionality to run.
	 */
	public void runSynchronized(Runnable runnable);
	
	/**
	 * Gives handlers access to the server context.
	 * 
	 * @return The server context for this MBean.
	 */
	public ServerContext getContext();
	
	public RemoteOddjobBean getRemoteBean();
	
	public ServerSession getServerSession();
	
}