/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.monitor.context.AncestorContext;

/**
 *  Provide a server context which can be passed down through the nodes
 *  of the server and used to look up useful things.
 *  
 *  @author Rob Gordon.
 */
public interface ServerContext extends AncestorContext {

	/**
	 * Add a child component to the context.
	 * 
	 * @param child A child.
	 * 
	 * @return The new server context.
	 * 
	 * @throws ServerLoopBackException When the child has already been
	 * exported by a previous server in the hierarchy.
	 */
	ServerContext addChild(Object child) throws ServerLoopBackException;
	
	/**
	 * Get the model.
	 * 
	 * @return The model.
	 */
	ServerModel getModel();
	
	/**
	 * Get the log archiver for the component this is the context for.
	 * 
	 * @return A log archiver.
	 */
	LogArchiver getLogArchiver();
		
	/**
	 * Get the console archiver for the component this is the context for.
	 * 
	 * @return A console archiver.
	 */
	ConsoleArchiver getConsoleArchiver();

	/**
	 * Get the bean directory that the component this is the context for 
	 * belongs to. This is used to discover the address. It need not be exposed apart from
	 * children to search the hierarchy.
	 * 
	 * @return The bean directory.
	 */
	BeanDirectory getBeanDirectory();

	/**
	 * The address. This will only be known if the component and all it's parent
	 * {@link org.oddjob.arooa.registry.BeanDirectoryOwner}s have an id within their {@link BeanDirectory}.
	 * 
	 * @return The address of this component. Null if there is no path of id's to the component.
	 */
	Address getAddress();
	
	/**
	 * The server id.
	 * 
	 * @return The server id of this component.
	 */
	ServerId getServerId();
}
