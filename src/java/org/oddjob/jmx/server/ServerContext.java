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

	public ServerContext addChild(Object child) throws ServerLoopBackException;
	
	public ServerModel getModel();
	
	public LogArchiver getLogArchiver();
		
	public ConsoleArchiver getConsoleArchiver();

	public BeanDirectory getBeanDirectory();

	public Address getAddress();
	
	public ServerId getServerId();
}
