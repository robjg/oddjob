package org.oddjob.jmx.client;

import org.oddjob.Structural;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.server.ServerInfo;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * A wrapper for the server main bean.
 * 
 * @author rob
 *
 */
public class ServerView implements 
RemoteDirectoryOwner, RemoteOddjobBean {
	
	private final RemoteDirectoryOwner remoteDirectoryOwner;
	private final RemoteOddjobBean remoteBean;

	public ServerView(Object serverMainProxy) {
		this.remoteDirectoryOwner = (RemoteDirectoryOwner) serverMainProxy;
		this.remoteBean = (RemoteOddjobBean) serverMainProxy;
		
	}

	public void startStructural(
			final ChildHelper<Object> childHelper) {
		
		Structural structural = (Structural) remoteDirectoryOwner;
		
		structural.addStructuralListener(new StructuralListener() {
			public void childAdded(StructuralEvent event) {
				childHelper.insertChild(
						event.getIndex(), 
						event.getChild());
			}
			public void childRemoved(StructuralEvent event) {
				childHelper.removeChildAt(
						event.getIndex());
			}
		});
	}
	
	public RemoteDirectory provideBeanDirectory() {
		return remoteDirectoryOwner.provideBeanDirectory();
	}
	
	public Object getProxy() {
		return remoteDirectoryOwner;
	}
	
	public ServerInfo serverInfo() {
		return remoteBean.serverInfo();
	}
	
	public void noop() {
		remoteBean.noop();
	}
	
}
