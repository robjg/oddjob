package org.oddjob.jmx.client;

import org.oddjob.arooa.ClassResolver;
import org.oddjob.framework.Exportable;
import org.oddjob.framework.Transportable;
import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.server.ServerInfo;
import org.oddjob.util.ClassLoaderSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

/**
 * The client side representation of a remote node. A proxy is used to implement
 * a mirror of the remote node. This class is the invocation handler for that
 * proxy. This class is never accessed directly by client code.
 * <p>
 * On creation the client node will lookup up various things on the server
 * on configure the proxy, register for notifications and start a resync.
 * <p>
 * It is possible that a serverside node has bean created and destroyed and
 * that the client hasn't caught up. In this case dead placeholder nodes are
 * put in the tree. They should be short lived, and removed when the client
 * catches up with the notifications. 
 * 
 * @author Rob Gordon
 */

public class ClientNode implements InvocationHandler, Exportable {
	private static final Logger logger = LoggerFactory.getLogger(ClientNode.class);
	
	/** The name of the mbean this node represents. */
	private final long remoteId;

	/** Save the proxy object created to shadow the remote node. */
	private final Object proxy;

	private final ClientInterfaceManager interfaceManager;
	
	/**
	 * Constructor.
	 * 
	 * @param remoteId
	 *            The name of the mbean were monitoring.
	 * @param toolkit
	 *            The connection to the remote server.
	 * 
	 * @throws Exception
	 *             if anything goes wrong.
	 */
	private ClientNode(long remoteId,
			ClientSideToolkit toolkit) {

		this.remoteId = remoteId;

		RemoteOddjobBean remote =
				new DirectInvocationClientFactory<>(
						RemoteOddjobBean.class).createClientHandler(
							null, toolkit);
		
		ServerInfo serverInfo = remote.serverInfo();
		
		ClassResolver classResolver =  
			toolkit.getClientSession().getArooaSession(
					).getArooaDescriptor().getClassResolver();
		
		Class<?>[] classesRemoteSupports = new ResolverHelper(classResolver
				).resolve(serverInfo.getInterfaces());

		ClientInterfaceManagerFactory managerFactory =
				toolkit.getClientSession().getInterfaceManagerFactory();

		Class<?>[] interfaces = managerFactory.filter(classesRemoteSupports);
		
		this.proxy = Proxy.newProxyInstance(
				new ClassLoaderSorter().getTopLoader(interfaces),
				interfaces, 
				this);

		// create the ClientInterfaceManager
		interfaceManager = managerFactory.create(
				proxy, 
				toolkit);
		
		logger.debug("Client Node creation complete [" +
				proxy.toString() + "], objectName=" + remoteId);
	}

	/**
	 * Static factory method.
	 * 
	 * @param objectName
	 *            The remote node.
	 * @param toolkit
	 *            The server connection.
	 * @return A proxy object that implements it's interfaces.
	 * 
	 * @throws RemoteException
	 */
	public static Handle createProxyFor(long objectName,
			ClientSideToolkit toolkit) {
		
		ClientNode client = new ClientNode(
				objectName,
				toolkit);
				
		return client.new Handle();
	}
	
	/**
	 * Called by the proxy to invoke a method.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) 
	throws Throwable {
		return interfaceManager.invoke(method, args);
	}

	public String toString() {
		return "ClientNode: " + remoteId;
	}
	
	/**
	 * Part of the implementation of the HostRelative interface. This is called when
	 * the proxy is just about to be sent over the network.
	 * 
	 * @return The object for transit.
	 */
	public Transportable exportTransportable() {
		
		logger.debug("[" + proxy + "] exported with remote Id [" + remoteId + "]");
		return new ComponentTransportable(remoteId);
	}
	
	public class Handle {
		public Object getProxy() {
			return proxy;
		}
		public Destroyable getDestroyer() {
			return interfaceManager;
		}
	}
	
}
