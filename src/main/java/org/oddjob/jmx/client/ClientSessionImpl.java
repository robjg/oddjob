package org.oddjob.jmx.client;

import org.oddjob.arooa.ArooaSession;
import org.slf4j.Logger;

import javax.management.MBeanServerConnection;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Simple implementation of a {@link ClientSession}
 * 
 * @author rob
 *
 */
public class ClientSessionImpl implements ClientSession {

	private final Logger logger;
	
	private final Map<Object, Long> names = new HashMap<>();
	
	private final Map<Long, Object> proxies = new HashMap<>();

	private final Map<Object, Destroyable> destroyers =
			new HashMap<>();
	
	private final ArooaSession arooaSession;
	
	private final MBeanServerConnection serverConnection;

	private final ScheduledExecutorService notificationProcessor;

	private final ClientInterfaceManagerFactory interfaceManagerFactory;

	/**
	 * Constructor.
	 * 
	 * @param serverConnection The server connection.
	 * @param notificationProcessor The notification processor.
	 * @param arooaSession The local session.
	 * @param logger The logger.
	 */
	public ClientSessionImpl(
			MBeanServerConnection serverConnection,
			ScheduledExecutorService notificationProcessor,
			ClientInterfaceManagerFactory interfaceManagerFactory,
			ArooaSession arooaSession,
			Logger logger) {
		this.serverConnection = Objects.requireNonNull(serverConnection);
		this.notificationProcessor = Objects.requireNonNull(notificationProcessor);
		this.interfaceManagerFactory = Objects.requireNonNull(interfaceManagerFactory);
		this.arooaSession = Objects.requireNonNull(arooaSession);
		this.logger = Objects.requireNonNull(logger);
	}

	@Override
	public Object create(long remoteId) {

		Object childProxy = proxies.get(remoteId);
		
		if (childProxy != null) {
			return childProxy;
		}

		try {
			ClientSideToolkitImpl toolkit = new ClientSideToolkitImpl(remoteId, this);
			
			ClientNode.Handle handle = ClientNode.createProxyFor(remoteId,
					toolkit);
			childProxy = handle.getProxy();
			destroyers.put(childProxy, handle.getDestroyer());
		} 
		catch (Exception e) {
			logger.error("Failed creating client node for [" + remoteId +
					"].", e);
			
			// Must return something.
			return new NodeCreationFailed(e);
		}
		
		names.put(childProxy, remoteId);
		proxies.put(remoteId, childProxy);
		
		return childProxy;
	}

	@Override
	public long idFor(Object proxy) {
		return Optional.ofNullable(names.get(proxy)).orElse(-1L);
	}
	
	@Override
	public Object objectFor(long objectId) {
		return proxies.get(objectId);
	}
	
	@Override
	public void destroy(Object proxy) {
		Destroyable destroyer = destroyers.get(proxy);
		destroyer.destroy();
		long name = names.remove(proxy);
		proxies.remove(name);
	}
	
	@Override
	public ArooaSession getArooaSession() {
		return arooaSession;
	}
	
	@Override
	public Logger logger() {
		return logger;
	}
	
	public MBeanServerConnection getServerConnection() {
		return serverConnection;
	}
	
	public ScheduledExecutorService getNotificationProcessor() {
		return notificationProcessor;
	}

	@Override
	public ClientInterfaceManagerFactory getInterfaceManagerFactory() {
		return interfaceManagerFactory;
	}

	@Override
	public void destroyAll() {
		List<Object> proxies = new ArrayList<>(names.keySet());
		for (Object proxy : proxies) {
			destroy(proxy);
		}
	}
}
