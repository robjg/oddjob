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
import org.oddjob.monitor.context.AncestorContext;
import org.oddjob.util.ThreadManager;

/**
 * This is a {@link ServerContext} for the {@link ServerMainBean}.
 * <p>
 *     TODO: ServerContexts should be refactored so this can just be an {@link ServerContextImpl}.
 * </p>
 */
public class ServerContextMain implements ServerContext {

	private final MainModel model;
	
	private final ServerMainBean serverMainBean;
	
	/**
	 * A constructor for the top most server 
	 * context.
	 */
	public ServerContextMain( 
			ServerModel model,
			ServerMainBean serverMainBean) {
		
		this.model = new MainModel(model);
		
		this.serverMainBean = serverMainBean;
	}

	@Override
	public Object getThisComponent() {
		return serverMainBean;
	}

	@Override
	public AncestorContext getParent() {
		return null;
	}

	@Override
	public ServerContext addChild(Object child) {
		return new ServerContextImpl(child, model.model, this);
	}

	@Override
	public ServerModel getModel() {
		return model;
	}

	@Override
	public LogArchiver getLogArchiver() {
		return null;
	}

	@Override
	public ConsoleArchiver getConsoleArchiver() {
		return null;
	}

	public ServerId getServerId() {
		return model.getServerId();
	}

	@Override
	public Address getAddress() {
		return null;
	}

	@Override
	public BeanDirectory getBeanDirectory() {
		return serverMainBean.provideBeanDirectory();
	}

	/**
	 * Not sure why we need this.
	 */
	static class MainModel implements ServerModel {

		private final ServerModel model;

		public MainModel(ServerModel model) {
			this.model = model;
		}

		@Override
		public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
			return new ServerInterfaceManagerFactoryImpl(
					new ServerInterfaceHandlerFactory<?, ?>[] {
							new RemoteOddjobHandlerFactory(),
							new ObjectInterfaceHandlerFactory(),
							new BeanDirectoryHandlerFactory(),
							new StructuralHandlerFactory()
					});
		}

		@Override
		public String getLogFormat() {
			return model.getLogFormat();
		}

		@Override
		public ServerId getServerId() {
			return model.getServerId();
		}

		@Override
		public ThreadManager getThreadManager() {
			return model.getThreadManager();
		}
	}
}
