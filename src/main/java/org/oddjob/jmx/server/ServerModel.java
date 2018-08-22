/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.ServerId;
import org.oddjob.util.ThreadManager;

/**
 *  Collects properties of the server in one place.
 *  
 *  @author Rob Gordon.
 */
public interface ServerModel {

	/**
	 * @return Returns the uniqueId.
	 */
	public ServerId getServerId();
			
	/**
	 * @return Returns the threadManager.
	 */
	public ThreadManager getThreadManager();
	
	/**
	 * 
	 * @return Returns the interfaceManagerFactory.
	 */
	public ServerInterfaceManagerFactory getInterfaceManagerFactory();
	

	/**
	 * Getter for log format.
	 * 
	 * @return The log format.
	 */
	public String getLogFormat();
}
