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
public class ServerModelImpl implements ServerModel {

	/** ThreadManager to use when executing jobs on the server. */
	private final ThreadManager threadManager;
	
	/** Interface Manager */
	private final ServerInterfaceManagerFactory imf;
	
	/** Server url */
	private final ServerId serverId;
	
	/** The log format to use when remembering log messages. */
	private String logFormat;
	
	/**
	 * A constructor for the top most server 
	 * context.
	 */
	public ServerModelImpl(ServerId serverId, 
			ThreadManager threadManager, 
			ServerInterfaceManagerFactory imf) {
		
		if (serverId == null) {
			throw new NullPointerException("Null URL.");
		}
		if (threadManager == null) {
			throw new NullPointerException("Null ThreadManager.");
		}
		if (imf == null) {
			throw new NullPointerException("Null ServerInterfaceManagerFactory.");
		}
		
		this.serverId = serverId;
		this.threadManager = threadManager;
		this.imf = imf;
	}

	
	/**
	 * @return Returns the uniqueId.
	 */
	public ServerId getServerId() {
		return serverId;
	}
			
	/**
	 * @return Returns the threadManager.
	 */
	public ThreadManager getThreadManager() {
		return threadManager;
	}
	
	/**
	 * 
	 * @return Returns the interfaceManagerFactory.
	 */
	public ServerInterfaceManagerFactory getInterfaceManagerFactory() {
		return imf;
	}
	
	/**
	 * Getter for log format.
	 * 
	 * @return The log format.
	 */
	public String getLogFormat() {
		return logFormat;
	}
	
	/**
	 * The log format.
	 * 
	 * @param logFormat The log format.
	 */
	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}
	
}
