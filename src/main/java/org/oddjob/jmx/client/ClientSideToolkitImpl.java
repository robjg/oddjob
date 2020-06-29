package org.oddjob.jmx.client;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.Utils;
import org.oddjob.jmx.general.RemoteBridge;
import org.oddjob.remote.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link ClientSideToolkit}.
 * 
 * @author rob
 *
 */
class ClientSideToolkitImpl implements ClientSideToolkit {
	private static final Logger logger = LoggerFactory.getLogger(ClientSideToolkitImpl.class);

	private final static int ACTIVE = 0;

	private final static int DESTROYED = 3;
	
	private volatile int phase = ACTIVE;

	private final ClientSessionImpl clientSession;
	
	private final ObjectName objectName;
	
	private final Map<String, NotificationListener> notifications 
	= new LinkedHashMap<>();

	/** The listener that listens for all JMX notifications. */
	private final ClientListener clientListener;

		
	public ClientSideToolkitImpl(ObjectName objectName, 
			ClientSessionImpl clientSession) throws InstanceNotFoundException, IOException {

		this.clientSession = Objects.requireNonNull(clientSession);
		this.objectName = Objects.requireNonNull(objectName);

		clientListener = new ClientListener();
		
		clientSession.getServerConnection().addNotificationListener(objectName,
				clientListener, null, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T invoke(RemoteOperation<T> remote, Object... args) throws Throwable {
		Objects.requireNonNull(remote);

		Object[] exported = Utils.export(args);

		Object result;
		try {
			result = clientSession.getServerConnection().invoke(
					objectName, 
					remote.getActionName(), 
					exported,
					remote.getSignature());
		} catch (ReflectionException e) {
			throw e.getTargetException();
		} catch (MBeanException e) {
			throw e.getTargetException();
		}
		return (T) Utils.importResolve(result, clientSession);

	}
	
	public void registerNotificationListener(String eventType, NotificationListener notificationListener) {
		notifications.put(eventType, notificationListener);
	}

	public void removeNotificationListener(String eventType,
			NotificationListener notificationListener) {
		notifications.remove(eventType);
	}
	
	public ClientSession getClientSession() {
		return clientSession;
	}
	
	/**
	 * Destroy this node. Clean up resources, remove remote connections.
	 */
	void destroy() {
		phase = DESTROYED;
		// beware the order here. 
		// notifications removed first
		try {
			// will fail if destroy is due to the remote node being removed.
			if (clientListener != null) {
				clientSession.getServerConnection().removeNotificationListener(objectName,
						clientListener);
			}
		} catch (JMException | IOException e) {
			logger.debug("Client destroy.", e);
		}
		
		logger.debug("Destroyed client for [" + toString() + "]");
	}

	@Override
	public String toString() {
		return "Client: " + objectName;
	}
	
	/**
	 * Member class which listens for notifications coming 
	 * across the network.
	 *
	 */
	class ClientListener implements javax.management.NotificationListener {
		
		// do notifications always come on one thread? should we synchronze just in case they don't?
		public void handleNotification(
				final javax.management.Notification notification,
				final Object object) {
			
			String type = notification.getType();
			logger.debug("Handling notification [" + type + "] sequence [" +
					notification.getSequenceNumber() + "] for [" + 
					ClientSideToolkitImpl.this.toString() + "]");

			if (phase == DESTROYED) {
				logger.debug("Ignoring notification as client destroyed [" + 
						ClientSideToolkitImpl.this.toString() + "]");
				return;
			}
			
			final NotificationListener listener = 
				 notifications.get(type);
			
			if (listener != null) {
				Runnable r = () -> {
					try {
						listener.handleNotification(RemoteBridge.fromJmxNotification(notification));
					} catch (Exception e) {
						// this will happen when the remote node disappears
						logger.debug("Handle notification.", e);
					}
				};
				clientSession.getNotificationProcessor().submit(r);
			}
						
		} // handleNotification
		
		@Override
		public String toString() {
			return ClientSideToolkitImpl.this.toString();
		}
	}

}
