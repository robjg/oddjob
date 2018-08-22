/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.Address;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;

/**
 *  Provide a server context which can be passed down through the nodes
 *  of the server and used to look up useful things.
 *  
 *  @author Rob Gordon.
 */
public interface ServerContext {

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
	public ServerContext addChild(Object child) throws ServerLoopBackException;
	
	/**
	 * Get the model.
	 * 
	 * @return The model.
	 */
	public ServerModel getModel();
	
	/**
	 * Get the log archiver for the component this is the context for.
	 * 
	 * @return A log archiver.
	 */
	public LogArchiver getLogArchiver();
		
	/**
	 * Get the console archiver for the component this is the context for.
	 * 
	 * @return A console archiver.
	 */
	public ConsoleArchiver getConsoleArchiver();

	/**
	 * Get the bean directory that the component this is the context for 
	 * belongs to.
	 * 
	 * @return The bean directory.
	 */
	public BeanDirectory getBeanDirectory();

	/**
	 * The address.
	 * 
	 * @return The address of this component.
	 */
	public Address getAddress();
	
	/**
	 * The server id.
	 * 
	 * @return The server id of this component.
	 */
	public ServerId getServerId();
}
