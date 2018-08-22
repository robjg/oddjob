/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx;

import org.oddjob.jmx.server.ServerInfo;

/**
 * A RemoteOddjobBean defines an interface a client can use to get
 * information about the bean and request a resync.
 *
 */
public interface RemoteOddjobBean {
	
	/**
	 * Get the component info.
	 * 
	 * @return ServerInfo for the component.
	 */
	public ServerInfo serverInfo();
	

	/**
	 * For heart beats.
	 */
	public void noop();
}
