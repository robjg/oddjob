package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.jmx.handlers.BeanDirectoryHandlerFactory;
import org.oddjob.jmx.handlers.ObjectInterfaceHandlerFactory;
import org.oddjob.jmx.handlers.RemoteOddjobHandlerFactory;
import org.oddjob.jmx.handlers.StructuralHandlerFactory;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.util.ThreadManager;

public class ServerContextMain implements ServerContext {

	private final ServerModel model;
	
	private final BeanDirectory beanDirectory;
	
	private final ServerId serverId;
	
	/**
	 * A constructor for the top most server 
	 * context.
	 */
	public ServerContextMain( 
			ServerModel model,
			BeanDirectory componentRegistry) {
		
		this.model = model;
		
		this.beanDirectory = componentRegistry;

		this.serverId = model.getServerId();

	}

	public ServerContext addChild(Object child) throws ServerLoopBackException {
		return new ServerContextImpl(child, model, beanDirectory);
	}

	public ServerModel getModel() {
		return new MainModel();
	}
	
	public LogArchiver getLogArchiver() {
		return null;
	}
		
	public ConsoleArchiver getConsoleArchiver() {
		return null;
	}
	
	public ServerId getServerId() {
		return serverId;
	}

	public Address getAddress() {
		return null;
	}

	public BeanDirectory getBeanDirectory() {
		return beanDirectory;
	}
	
	class MainModel implements ServerModel {
		
		public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
			return new ServerInterfaceManagerFactoryImpl(
					new ServerInterfaceHandlerFactory<?, ?>[] {
							new RemoteOddjobHandlerFactory(),
							new ObjectInterfaceHandlerFactory(),
							new BeanDirectoryHandlerFactory(),
							new StructuralHandlerFactory()
					});
		}
		
		public String getLogFormat() {
			return model.getLogFormat();
		}
		
		public ServerId getServerId() {
			return model.getServerId();
		}
		
		public ThreadManager getThreadManager() {
			return model.getThreadManager();
		}
	}
}
