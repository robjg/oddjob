package org.oddjob.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

import org.oddjob.framework.SimpleJob;

/**
 * @oddjob.description A job which creates an RMI registry.
 *
 * @author Rob Gordon
 */
public class RMIRegistryJob extends SimpleJob {

	/** The default port */
	public static final int DEFAULT_PORT = 1099;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The port to use 
	 * @oddjob.required No. Defaults to 1099.
	 */
	private int port = DEFAULT_PORT;

	/**
	 * Set the port number to use.
	 * 
	 * @param port The port number.
	 */
	synchronized public void setPort(int port) {		
		this.port = port;
	}

	/**
	 * Get the port number.
	 * 
	 * @return The port number.
	 */
	synchronized public int getPort() {		
		return this.port;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	public int execute() throws Exception {
		try {
			LocateRegistry.createRegistry(getPort());
		} catch (ExportException e) {
			logger().info("Registry probably exists already: " + e.getMessage());
		}

		return 0;	
	}
}
