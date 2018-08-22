package org.oddjob.jmx.server;

import org.oddjob.arooa.registry.ServerId;

public class ServerLoopBackException extends Exception {
	private static final long serialVersionUID = 2009061800L;
	
	private final ServerId serverId;
	
	public ServerLoopBackException(ServerId serverId) {
		super("Loop back detected:" + serverId);
		this.serverId = serverId;
	}
	
	public ServerId getServerId() {
		return serverId;
	}
}
