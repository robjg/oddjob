package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.*;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

import javax.management.*;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

/**
 */
public class ObjectInterfaceHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, Object> {

	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	private static final JMXOperationPlus<String> TO_STRING =
			new JMXOperationPlus<>(
					"toString",
					"toString.",
					String.class,
					MBeanOperationInfo.INFO);
	
	public Class<Object> interfaceClass() {
		return Object.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			TO_STRING.getOpInfo() };
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}
	
	
	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit ojmb) {
		return new ServerObjectHandler(target);
	}

	public ClientHandlerResolver<Object> clientHandlerFactory() {
		return new SimpleHandlerResolver<>(
				ClientObjectHandlerFactory.class.getName(),
				VERSION);
	}
	
	public static class ClientObjectHandlerFactory implements ClientInterfaceHandlerFactory<Object> {
		
		public Class<Object> interfaceClass() {
			return Object.class;
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}

		public Object createClientHandler(Object proxy, ClientSideToolkit toolkit) {
			return new ClientObjectHandler(proxy, toolkit);
		}
	}
	
	static class ClientObjectHandler {

		private final ClientSideToolkit toolkit;
		
		private final Object proxy;
		
		/** Save the remote toString value */
		private String toString;
		
		ClientObjectHandler(Object proxy, ClientSideToolkit toolkit) {
			this.proxy = proxy;
			this.toolkit = toolkit;
		}
		
		public String toString() {
			if (toString == null) {
				try {
					toString = toolkit.invoke(TO_STRING);
				} catch (Throwable t) {
					throw new UndeclaredThrowableException(t);
				}
				if (toString == null) {
					toString = "null";
				}
			}
			return toString;
		}
		
		public boolean equals(Object other) {
			return (other == proxy);
		}

		@Override
		public int hashCode() {
			return Proxy.getInvocationHandler(proxy).hashCode();
		}
	}
	
	static class ServerObjectHandler implements ServerInterfaceHandler {
	
		private final Object object;
		
		ServerObjectHandler(Object object) {
			this.object = object;
		}
		
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
		
		public Notification[] getLastNotifications() {
			return null;
		}
		
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