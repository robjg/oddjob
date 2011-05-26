/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;

/**
 * Simple Implementation of an InterfaceManager.
 *
 * @author Rob Gordon
 */
public class ServerInterfaceManagerImpl implements ServerInterfaceManager {

	/** The collective mBeanInfo */
	private final MBeanInfo mBeanInfo;

	/** The collective interfaces. */
	private final List<ClientHandlerResolver> clientResolvers = 
		new ArrayList<ClientHandlerResolver>();
	
	private final ServerInterfaceHandler[] handlers;
	
	
	/** Map of methods to InterfaceHandlers. Not sure if the order
	 * interface might be important but we are using a LinkedHashMap just
	 * in case it is. */
	private final Map<RemoteOperation<?>, ServerInterfaceHandler> operations = 
		new LinkedHashMap<RemoteOperation<?>, ServerInterfaceHandler>();
	
	/**
	 * Constructor.
	 * 
	 * @param target. The target object the OddjobMBean is representing.
	 * @param ojmb The OddjobMBean.
	 * @param serverHandlerFactories The InterfaceInfos.
	 */
	public ServerInterfaceManagerImpl(Object target, 
			ServerSideToolkit ojmb,
			ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
		
		List<MBeanAttributeInfo> attributeInfo = 
			new ArrayList<MBeanAttributeInfo>();
		List<MBeanOperationInfo> operationInfo = 
			new ArrayList<MBeanOperationInfo>();
		List<MBeanNotificationInfo> notificationInfo = 
			new ArrayList<MBeanNotificationInfo>();
		
		handlers = new ServerInterfaceHandler[serverHandlerFactories.length];
		
		// Loop over all definitions.
		for (int i = 0; i < serverHandlerFactories.length; ++i) {
			ServerInterfaceHandlerFactory<?, ?> serverHandlerFactory = serverHandlerFactories[i];
	
			clientResolvers.add(serverHandlerFactory.clientHandlerFactory());
			
			// create the interface handler
			ServerInterfaceHandler interfaceHandler 
				= create(target, ojmb, serverHandlerFactory);
			handlers[i] = interfaceHandler;
	
			// collate MBeanAttributeInfo.
			attributeInfo.addAll(Arrays.asList(serverHandlerFactory.getMBeanAttributeInfo()));
			
			// collate MBeanOperationInfo.
			MBeanOperationInfo[] oInfo = serverHandlerFactory.getMBeanOperationInfo();
			for (int j = 0; j < oInfo.length; ++j) {
				operationInfo.add(oInfo[j]);
				operations.put(
						new OperationInfoOperation(oInfo[j]), 
						interfaceHandler);
			}
			
			// collate MBeanNotificationInfo.
			notificationInfo.addAll(Arrays.asList(serverHandlerFactory.getMBeanNotificationInfo()));			
		}
		
		// create an MBeanInfo from the collated informations.
		mBeanInfo = new MBeanInfo(target.toString(), 
				"Description of " + target.toString(),
				(MBeanAttributeInfo[]) attributeInfo.toArray(new MBeanAttributeInfo[0]),
				new MBeanConstructorInfo[0], 
				(MBeanOperationInfo[]) operationInfo.toArray(new MBeanOperationInfo[0]), 
				(MBeanNotificationInfo[]) notificationInfo.toArray(new MBeanNotificationInfo[0]));
	}

	private <S> ServerInterfaceHandler create(
			Object target, 
			ServerSideToolkit ojmb,
			ServerInterfaceHandlerFactory<S, ?> factory) {
		
		Class<S> type = factory.interfaceClass();
		
		if (! type.isInstance(target)) {
			throw new ClassCastException("" + target + 
					" not of type " + type.getName());
		}
		
		// create the interface handler
		ServerInterfaceHandler interfaceHandler 
			= factory.createServerHandler(
					type.cast(target), ojmb);
		
		return interfaceHandler;
	}
	
	public ClientHandlerResolver[] allClientInfo() {
		return clientResolvers.toArray(
				new ClientHandlerResolver[clientResolvers.size()]);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceManager#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo() {
		return mBeanInfo;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceManager#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(String actionName, 
			Object[] params, String[] signature)
	throws MBeanException, ReflectionException {
		RemoteOperation<Object> op = new MBeanOperation(actionName, signature);
		
		ServerInterfaceHandler interfaceHandler = operations.get(op);
		if (interfaceHandler == null) {
			throw new IllegalArgumentException(
					"No interface supports method [" + op + "]");
		}
		return interfaceHandler.invoke(op, params);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jmx.server.InterfaceManager#destroy()
	 */
	public void destroy() {
		for (int i = 0; i < handlers.length; ++i) {
			handlers[i].destroy();
			handlers[i] = null;
		}
	}
}