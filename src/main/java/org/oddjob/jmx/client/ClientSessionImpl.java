package org.oddjob.jmx.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.oddjob.arooa.ArooaSession;
import org.slf4j.Logger;

/**
 * Simple implementation of a {@link ClientSession}
 * 
 * @author rob
 *
 */
public class ClientSessionImpl implements ClientSession {

	private final Logger logger;
	
	private final Map<Object, ObjectName> names
		= new HashMap<Object, ObjectName>();
	
	private final Map<ObjectName, Object> proxies
	= new HashMap<ObjectName, Object>();

	private final Map<Object, Destroyable> destroyers =
			new HashMap<Object, Destroyable>();
	
	private final ArooaSession arooaSession;
	
	private final MBeanServerConnection serverConnection;

	private final ScheduledExecutorService notificationProcessor;

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
			ArooaSession arooaSession,
			Logger logger) {
		this.serverConnection = serverConnection;
		this.notificationProcessor = notificationProcessor;
		this.arooaSession = arooaSession;
		this.logger = logger;
	}
	
	public Object create(ObjectName objectName) {

		Object childProxy = proxies.get(objectName);
		
		if (childProxy != null) {
			return childProxy;
		}
	
		try {
			ClientSideToolkitImpl toolkit = new ClientSideToolkitImpl(objectName, this);
			
			ClientNode.Handle handle = ClientNode.createProxyFor(objectName,
					toolkit);
			childProxy = handle.getproxy();
			destroyers.put(childProxy, handle.getDestroyer());
		} 
		catch (Exception e) {
			logger.error("Failed creating client node for [" + objectName + 
					"].", e);
			
			// Must return something.
			return new NodeCreationFailed(e);
		}
		
		names.put(childProxy, objectName);
		proxies.put(objectName, childProxy);
		
		return childProxy;
	}

	@Override
	public ObjectName nameFor(Object proxy) {
		return names.get(proxy);
	}
	
	@Override
	public Object objectFor(ObjectName name) {
		return proxies.get(name);
	}
	
	@Override
	public void destroy(Object proxy) {
		Destroyable destroyer = destroyers.get(proxy);
		destroyer.destroy();
		ObjectName name = names.remove(proxy);
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
	public void destroyAll() {
		List<Object> proxies = new ArrayList<Object>(names.keySet());
		for (Object proxy : proxies) {
			destroy(proxy);
		}
	}
}
