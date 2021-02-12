/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.OJConstants;
import org.oddjob.arooa.registry.*;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.appender.AppenderArchiver;
import org.oddjob.logging.cache.LocalConsoleArchiver;
import org.oddjob.monitor.context.AncestorContext;

/**
 *  Provide a server context which can be passed down through the nodes
 *  of the server and used to look up useful things.
 *  
 *  @author Rob Gordon.
 */
public class ServerContextImpl implements ServerContext {

	private final ServerContext parent;

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
			ServerContext parent) {

		this.model = model;
		this.parent = parent;
		this.node = root;
		
		String logFormat = model.getLogFormat();
		logArchiver = new AppenderArchiver(root, 
				logFormat == null ? OJConstants.DEFAULT_LOG_FORMAT : logFormat);
		consoleArchiver = new LocalConsoleArchiver();

		this.beanDirectory = parent.getBeanDirectory();

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
		
		this.parent = parent;
		this.model = parent.model;
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
		Path path = parent.path;

		if (parent.node instanceof BeanDirectoryOwner) {
			this.beanDirectory = ((BeanDirectoryOwner) parent.node).provideBeanDirectory();
			
			if (beanDirectory == null) {
				throw new IllegalStateException("" + parent.node + 
						" has no registry.");
			}

			if (beanDirectory instanceof RemoteDirectory) {
				serverId = ((RemoteDirectory) beanDirectory).getServerId();
				ServerId rootServerId = parent.getModel().getServerId();
				if (serverId.equals(rootServerId)) {
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
			this.beanDirectory = parent.beanDirectory;
		}

		this.path = path;
		this.serverId = serverId;
		this.id = beanDirectory.getIdFor(node);
	}

	@Override
	public Object getThisComponent() {
		return node;
	}

	@Override
	public AncestorContext getParent() {
		return parent;
	}

	@Override
	public ServerContext addChild(Object child)
	throws ServerLoopBackException {
		return new ServerContextImpl(child, this);
	}

	@Override
	public ServerModel getModel() {
		return model;
	}


	@Override
	public LogArchiver getLogArchiver() {
		return logArchiver;
	}

	@Override
	public ConsoleArchiver getConsoleArchiver() {
		return consoleArchiver;
	}

	@Override
	public ServerId getServerId() {
		return serverId;
	}

	@Override
	public Address getAddress() {
		if (id != null && path != null) {
			return new Address(serverId, path.addId(id));
		}
		else {
			return null;
		}
	}

	@Override
	public BeanDirectory getBeanDirectory() {
		return beanDirectory;
	}
}
