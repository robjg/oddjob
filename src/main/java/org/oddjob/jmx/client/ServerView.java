package org.oddjob.jmx.client;

import org.oddjob.Structural;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.server.ServerInfo;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

import java.util.Arrays;
import java.util.Objects;

/**
 * A wrapper for the client side proxy of the {@link org.oddjob.jmx.server.ServerMainBean}.
 * 
 * @author rob
 *
 */
public class ServerView implements RemoteDirectoryOwner, RemoteOddjobBean {

	private final RemoteDirectoryOwner remoteDirectoryOwner;

	private final RemoteOddjobBean remoteBean;

	public ServerView(Object serverMainProxy) {
		Objects.requireNonNull(serverMainProxy, "No Server Main Proxy");

		if (serverMainProxy instanceof RemoteDirectoryOwner) {
			this.remoteDirectoryOwner = (RemoteDirectoryOwner) serverMainProxy;
		}
		else {
			throw new IllegalArgumentException("ServerMainProxy does not implement " +
					RemoteDirectoryOwner.class + ", toString=" + serverMainProxy +
					", implements " + Arrays.toString(serverMainProxy.getClass().getInterfaces()));
		}

		if (serverMainProxy instanceof RemoteOddjobBean) {
			this.remoteBean = (RemoteOddjobBean) serverMainProxy;
		}
		else {
			throw new IllegalArgumentException("ServerMainProxy does not implement " +
					RemoteOddjobBean.class + ", toString=" + serverMainProxy +
					", implements " + Arrays.toString(serverMainProxy.getClass().getInterfaces()));
		}
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
