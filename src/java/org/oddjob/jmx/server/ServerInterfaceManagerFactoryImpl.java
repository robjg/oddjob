/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.oddjob.jmx.SharedConstants;


public class ServerInterfaceManagerFactoryImpl
implements ServerInterfaceManagerFactory {
	
	private Set<ServerInterfaceHandlerFactory<?, ?>> serverHandlerFactories = 
		new HashSet<ServerInterfaceHandlerFactory<?, ?>>();
	
	public ServerInterfaceManagerFactoryImpl() {
		serverHandlerFactories.addAll(Arrays.asList(
				SharedConstants.DEFAULT_SERVER_HANDLER_FACTORIES));
	}
	
	public ServerInterfaceManagerFactoryImpl(ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
		this.serverHandlerFactories.addAll(Arrays.asList(serverHandlerFactories));
	}
	
	public void addServerHandlerFactories(ServerInterfaceHandlerFactory<?, ?>[] serverHandlerFactories) {
		if (serverHandlerFactories == null) {
			return;
		}
		this.serverHandlerFactories.addAll(Arrays.asList(serverHandlerFactories));
	}
	
	
	public ServerInterfaceManager create(Object target, ServerSideToolkit serverSideToolkit) {
		List<ServerInterfaceHandlerFactory<?, ?>> handlers = 
			new ArrayList<ServerInterfaceHandlerFactory<?, ?>>();

		// build up a list of supported interfaces
		for (Iterator<ServerInterfaceHandlerFactory<?, ?>> it = serverHandlerFactories.iterator(); it.hasNext(); ) {
			ServerInterfaceHandlerFactory<?, ?> interfaceHandler = it.next();
			Class<?> handles = interfaceHandler.interfaceClass();
			if (handles.isInstance(target)) {
				handlers.add(interfaceHandler);
			}
		}
					
		// create the implementation
		ServerInterfaceManagerImpl imImpl = new ServerInterfaceManagerImpl(
				target, 
				serverSideToolkit, 
				(ServerInterfaceHandlerFactory[]) handlers.toArray(new ServerInterfaceHandlerFactory[0]));
		return imImpl;
	}
}
