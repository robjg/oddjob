/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.OJConstants;
import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.registry.Path;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.cache.LocalConsoleArchiver;
import org.oddjob.logging.log4j.Log4jArchiver;

/**
 *  Provide a server context which can be passed down through the nodes
 *  of the server and used to look up useful things.
 *  
 *  @author Rob Gordon.
 */
public class ServerContextImpl implements ServerContext {

	private final Object node;
	
	private final ServerModel model;
	
	private final BeanDirectory beanDirectory;
	
	private final LogArchiver logArchiver;
	private final ConsoleArchiver consoleArchiver;

	private final ServerId serverId;
	
	private final String id;

	private final Path path;
	
	/**
	 * A constructor for the top most server 
	 * context.
	 */
	public ServerContextImpl(Object root, 
			ServerModel model,
			BeanDirectory componentRegistry) {
		
		this.model = model;
		this.node = root;
		
		String logFormat = model.getLogFormat();
		logArchiver = new Log4jArchiver(root, 
				logFormat == null ? OJConstants.DEFAULT_LOG_FORMAT : logFormat);
		consoleArchiver = new LocalConsoleArchiver();

		this.beanDirectory = componentRegistry;

		this.id = beanDirectory.getIdFor(node);		
		this.serverId = model.getServerId();

		this.path = new Path();
	}

	/**
	 * Create a context with a parent.
	 * 
	 * @param parent The parent.
	 */
	private ServerContextImpl(Object node, ServerContextImpl parent) 
	throws ServerLoopBackException {
		
		this.model = parent.getModel();
		
		this.node = node;
		
		if (parent.node instanceof LogArchiver) {
			logArchiver = (LogArchiver) parent.node;
		}
		else {
			logArchiver = parent.getLogArchiver();
		}
		
		if (parent.node instanceof ConsoleArchiver) {
			consoleArchiver = (ConsoleArchiver) parent.node;
		}
		else {
			consoleArchiver = parent.getConsoleArchiver();
		}

		ServerId serverId = parent.getServerId();

		if (parent.node instanceof BeanDirectoryOwner) {
			this.beanDirectory = ((BeanDirectoryOwner) parent.node).provideBeanDirectory();
			
			if (beanDirectory == null) {
				throw new IllegalStateException("" + parent.node + 
						" has no registry.");
			}
			
			if (beanDirectory instanceof RemoteDirectory) {
				serverId = ((RemoteDirectory) beanDirectory).getServerId();
				if (serverId.equals(model.getServerId())) {
					throw new ServerLoopBackException(serverId);
				}
			}
			if (!serverId.equals(parent.serverId)) {
				path = new Path();
			}
			else if (parent.path != null && parent.id != null) {
				path = parent.path.addId(parent.id);
			}
			else {
				path = null;
			}
		}
		else {
			this.path = parent.path;
			this.beanDirectory = parent.beanDirectory;
		}
		
		this.serverId = serverId;
		this.id = beanDirectory.getIdFor(node);
	}
	
	public ServerContext addChild(Object child) 
	throws ServerLoopBackException {
		return new ServerContextImpl(child, this);
	}
	
	public Object getComponent() {
		return node;
	}
	
	public ServerModel getModel() {
		return model;
	}
	
	public LogArchiver getLogArchiver() {
		return logArchiver;
	}
		
	public ConsoleArchiver getConsoleArchiver() {
		return consoleArchiver;
	}
	
	public ServerId getServerId() {
		return serverId;
	}

	public Address getAddress() {
		if (id != null && path != null) {
			return new Address(serverId, path.addId(id));
		}
		else {
			return null;
		}
	}

	public BeanDirectory getBeanDirectory() {
		return beanDirectory;
	}
}
