package org.oddjob.jmx.client;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;

public class ClientSessionImpl implements ClientSession {

	private final Logger logger;
	
	private final Map<Object, ObjectName> names
		= new HashMap<Object, ObjectName>();
	
	private final Map<ObjectName, Object> proxies
	= new HashMap<ObjectName, Object>();

	private final ArooaSession arooaSession;
	
	private final MBeanServerConnection serverConnection;

	private final NotificationProcessor notificationProcessor;

	public ClientSessionImpl(
			MBeanServerConnection serverConnection,
			NotificationProcessor notificationProcessor,
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
			
			childProxy = ClientNode.createProxyFor(objectName,
					toolkit);
		} 
		catch (Exception e) {
			logger.error("Failed creating client node for [" + objectName + 
					"].", e);
			return null;
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
	
	public NotificationProcessor getNotificationProcessor() {
		return notificationProcessor;
	}
		
}
