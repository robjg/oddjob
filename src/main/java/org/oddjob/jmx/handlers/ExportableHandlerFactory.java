package org.oddjob.jmx.handlers;

import org.oddjob.framework.Exportable;
import org.oddjob.framework.Transportable;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.lang.reflect.Proxy;

/**
 * Provide Handlers for the {@link Exportable} interface.
 * <p>
 * This is a special handler because Exportable is a
 * fake client side interface.
 * 
 * @author rob
 */
public class ExportableHandlerFactory implements ServerInterfaceHandlerFactory<Object, Exportable> {

	public static final HandlerVersion VERSION = new HandlerVersion(3, 0);

	@Override
	public Class<Object> serverClass() {
		return Object.class;
	}

	@Override
	public Class<Exportable> clientClass() {
		return Exportable.class;
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
		return new MBeanOperationInfo[0];
	}

	@Override
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit toolkit) {
		return new ServerInterfaceHandler() {
			@Override
			public Object invoke(RemoteOperation<?> operation, Object[] params) {
				throw new IllegalArgumentException("Nothing should call this!");
			}

			@Override
			public void destroy() {
			}
		};
	}

	public static class ClientFactory implements ClientInterfaceHandlerFactory<Exportable> {

			/*
			 * (non-Javadoc)
			 * @see org.oddjob.jmx.server.ServerInterfaceHandlerFactory#interfaceClass()
			 */
			public Class<Exportable> interfaceClass() {
				return Exportable.class;
			}

			public HandlerVersion getVersion() {
				return VERSION;
			}

			public Exportable createClientHandler(Exportable proxy, ClientSideToolkit toolkit) {
				return new ClientExportableHandler(proxy);
			}

			static class ClientExportableHandler implements Exportable {

				private final Exportable invocationHandler;

				ClientExportableHandler(Exportable proxy) {
					invocationHandler = (Exportable) Proxy.getInvocationHandler(proxy);
				}

				public Transportable exportTransportable() {
					return invocationHandler.exportTransportable();
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
}