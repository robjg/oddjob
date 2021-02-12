package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.HasInitialisation;
import org.oddjob.remote.Initialisation;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

/**
 */
public class ObjectInterfaceHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, Object> {

	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	private static final JMXOperationPlus<String> TO_STRING =
			new JMXOperationPlus<>(
					"toString",
					"toString.",
					String.class,
					MBeanOperationInfo.INFO);

	@Override
	public Class<Object> serverClass() {
		return Object.class;
	}

	@Override
	public Class<Object> clientClass() {
		return Object.class;
	}

	@Override
	public HandlerVersion getHandlerVersion() {
		return VERSION;
	}

	@Override
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	@Override
	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			TO_STRING.getOpInfo() };
	}

	@Override
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit ojmb) {
		return new ServerObjectHandler(target);
	}

	public static class ClientFactory implements ClientInterfaceHandlerFactory<Object> {

		@Override
		public Class<Object> interfaceClass() {
			return Object.class;
		}

		@Override
		public HandlerVersion getVersion() {
			return VERSION;
		}

		@Override
		public Object createClientHandler(Object proxy, ClientSideToolkit toolkit,
										  Initialisation<?> initialisation) {

			if (String.class.isAssignableFrom(initialisation.getType())) {
				return new ClientObjectHandler(proxy, toolkit,
						(String) initialisation.getData());
			}
			else {
				throw new IllegalArgumentException("String initialisation data expected.");
			}
		}
	}
	
	static class ClientObjectHandler {

		private final Object proxy;
		
		/** Save the remote toString value */
		private final String toString;
		
		ClientObjectHandler(Object proxy, ClientSideToolkit toolkit, String toString) {
			this.proxy = proxy;
			this.toString = toString;
		}

		@Override
		public String toString() {
			return toString;
		}

		@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
		@Override
		public boolean equals(Object other) {
			return (other == proxy);
		}

		@Override
		public int hashCode() {
			return Proxy.getInvocationHandler(proxy).hashCode();
		}
	}
	
	static class ServerObjectHandler implements ServerInterfaceHandler, HasInitialisation<String>  {
	
		private final Object object;
		
		ServerObjectHandler(Object object) {
			this.object = object;
		}

		@Override
		public Initialisation<String> initialisation() {
			return new Initialisation<>(String.class,
					object.toString());
		}

		@Override
		public Object invoke(RemoteOperation<?> operation, Object[] params)
		throws MBeanException, ReflectionException {
			
			if (TO_STRING.equals(operation)) {
				return object.toString();
			}
			else {
				throw new ReflectionException(
						new IllegalStateException("invoked for an unknown method."), 
								operation.toString());				
			}
		}

		@Override
		public void destroy() {
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj.getClass() == this.getClass();
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}