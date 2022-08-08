package org.oddjob.jmx.client;

import org.oddjob.arooa.utils.Pair;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.Utils;
import org.oddjob.jmx.general.RemoteBridge;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.remote.NotificationListener;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ClientSideToolkit}.
 * 
 * @author rob
 *
 */
class ClientSideToolkitImpl implements ClientSideToolkit {
	private static final Logger logger = LoggerFactory.getLogger(ClientSideToolkitImpl.class);

	private final ClientSessionImpl clientSession;

	private final long remoteId;

	private final ObjectName objectName;

	private final RemoteBridge remoteBridge;

	private final Set<Pair<NotificationType<?>, NotificationListener<?>>> listeners = ConcurrentHashMap.newKeySet();
		
	public ClientSideToolkitImpl(long remoteId,
			ClientSessionImpl clientSession) {

		this.clientSession = Objects.requireNonNull(clientSession);
		this.remoteId = remoteId;

		this.objectName = OddjobMBeanFactory.objectName(remoteId);

		this.remoteBridge = new RemoteBridge(clientSession.getServerConnection());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T invoke(RemoteOperation<T> remote, Object... args) throws Throwable {
		Objects.requireNonNull(remote);

		logger.trace("Invoking {} on remote {}", remote, remoteId);

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

	@Override
	public <T> void registerNotificationListener(NotificationType<T> eventType,
												 NotificationListener<T> notificationListener) {
		try {
			this.remoteBridge.addNotificationListener(remoteId, eventType, notificationListener);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}

		listeners.add(Pair.of(eventType, notificationListener));
	}

	@Override
	public <T> void removeNotificationListener(NotificationType<T> eventType,
			NotificationListener<T> notificationListener) {
		try {
			this.remoteBridge.removeNotificationListener(remoteId, eventType, notificationListener);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}

		listeners.remove(Pair.of(eventType, notificationListener));
	}
	
	public ClientSession getClientSession() {
		return clientSession;
	}
	
	/**
	 * Destroy this node. Clean up resources, remove remote connections.
	 */
	void destroy() {
		try {
			remoteBridge.destroy(remoteId);
		} catch (RemoteException e) {
			logger.warn("Failed destroying [{}]", remoteId, e);
		}
		logger.debug("Destroyed client for [{}]", remoteId);
	}

	@Override
	public String toString() {
		return "Client: " + objectName;
	}

}
