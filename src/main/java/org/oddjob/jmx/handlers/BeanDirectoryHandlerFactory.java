/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.*;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.Notification;

import javax.management.*;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

public class BeanDirectoryHandlerFactory implements
		ServerInterfaceHandlerFactory<BeanDirectoryOwner, RemoteDirectoryOwner> {

	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	private static final JMXOperationPlus<ServerId> SERVER_ID =
			new JMXOperationPlus<>(
					"serverId",
					"Get ServerId.",
					ServerId.class,
					MBeanOperationInfo.INFO);

	private static final JMXOperationPlus<Long[]> LIST =
		new JMXOperationPlus<>(
				"beansList", 
				"List Beans.",
				Long[].class,
				MBeanOperationInfo.INFO);

	private static final JMXOperationPlus<String> ID_FOR =
			new JMXOperationPlus<>(
					"beansIdFor",
					"Id For ObjectName.",
					String.class,
					MBeanOperationInfo.INFO)
			.addParam("objectName", ObjectName.class, "The object name.");

	private static final JMXOperationPlus<Object> LOOKUP =
			new JMXOperationPlus<>(
					"beansLookup",
					"Object lookup.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("id", String.class, "The id.");

	private static final JMXOperationPlus<Object> LOOKUP_TYPE =
			new JMXOperationPlus<>(
					"beansLookupType",
					"Object lookup.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("id", String.class, "The id.")
			.addParam("type", Class.class, "The type.");

	public Class<BeanDirectoryOwner> interfaceClass() {
		return BeanDirectoryOwner.class;
	}

	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] { SERVER_ID.getOpInfo(),
				LIST.getOpInfo(), ID_FOR.getOpInfo(), LOOKUP.getOpInfo(),
				LOOKUP_TYPE.getOpInfo() };
	}

	public MBeanNotificationInfo[] getMBeanNotificationInfo() {

		return new MBeanNotificationInfo[0];
	}

	public ClientHandlerResolver<RemoteDirectoryOwner> clientHandlerFactory() {
		return new SimpleHandlerResolver<>(
				ClientBeanDirectoryHandlerFactory.class.getName(),
				VERSION);
	}

	public static class ClientBeanDirectoryHandlerFactory implements
			ClientInterfaceHandlerFactory<RemoteDirectoryOwner> {

		public Class<RemoteDirectoryOwner> interfaceClass() {
			return RemoteDirectoryOwner.class;
		}

		public HandlerVersion getVersion() {
			return VERSION;
		}
		
		public RemoteDirectoryOwner createClientHandler(RemoteDirectoryOwner ignored,
				ClientSideToolkit toolkit) {
			return new ClientBeanDirectoryHandler(toolkit);
		}
	}

	static class ClientBeanDirectoryHandler implements RemoteDirectoryOwner {

		private final ClientSideToolkit toolkit;

		ClientBeanDirectoryHandler(ClientSideToolkit toolkit) {
			this.toolkit = toolkit;
		}

		public RemoteDirectory provideBeanDirectory() {
			return new RemoteDirectory() {

				private ServerId serverId;

				public Object lookup(String path) {
					try {
						Object result = toolkit.invoke(LOOKUP, path);
						if (result instanceof Carrier) {
							return toolkit.getClientSession().create(
									((Carrier) result).getObjectName());
						} else {
							return result;
						}
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}

				public <T> T lookup(String path, Class<T> required) {
					try {
						Object result = toolkit.invoke(LOOKUP_TYPE, path, required);
						if (result instanceof Carrier) {
							result = toolkit.getClientSession().create(
									((Carrier) result).getObjectName());
						}
						return required.cast(result);
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}
		
				public String getIdFor(Object bean) {
					long objectName = toolkit.getClientSession().nameFor(bean);
		
					if (objectName < 0) {
						return null;
					}
		
					try {
						return toolkit.invoke(ID_FOR,
								objectName);
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
		
				}
		
				public <T> Iterable<T> getAllByType(Class<T> type) {
					try {
						Long[] names = toolkit.invoke(LIST,
								type);
						
						if (names == null) {
							return new ArrayList<>();
						}

						List<T> results = new ArrayList<>();
		
						for (long objectName : names) {
							Object object = toolkit.getClientSession().create(
									objectName);
							if (object == null) {
								continue;
							}
							results.add(type.cast(object));
						}
						return results;
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}
		
				public ServerId getServerId() {
					if (serverId == null) {
						try {
							this.serverId = toolkit.invoke(SERVER_ID);
						} catch (Throwable e) {
							throw new UndeclaredThrowableException(e);
						}
					}
					return serverId;
				}
			};
		}
	}

	public ServerInterfaceHandler createServerHandler(BeanDirectoryOwner directory,
			ServerSideToolkit serverToolkit) {
		return new ServerBeanDirectoryHandler(
				directory, serverToolkit);
	}

	static class ServerBeanDirectoryHandler implements ServerInterfaceHandler {

		private final BeanDirectoryOwner directoryOwner;
		private final ServerSideToolkit serverToolkit;

		ServerBeanDirectoryHandler(BeanDirectoryOwner directory,
				ServerSideToolkit ojmb) {
			this.directoryOwner = directory;
			this.serverToolkit = ojmb;
		}

		public Notification[] getLastNotifications() {
			return null;
		}

		public Object invoke(RemoteOperation<?> operation, Object[] params)
				throws MBeanException, ReflectionException, ArooaPropertyException {
			
			if (SERVER_ID.equals(operation)) {

				return serverToolkit.getContext().getServerId();
			}
			
			BeanDirectory directory = directoryOwner.provideBeanDirectory();
			if (directory == null) {
				return null;
			}
			
			if (LIST.equals(operation)) {
				Class<?> type = (Class<?>) params[0];
				Iterable<?> all = directory.getAllByType(type);

				List<Long> names = new ArrayList<>();

				for (Object object : all) {
					long name = serverToolkit.getServerSession().nameFor(object);
					if (name >= 0) {
						names.add(name);
					}
				}

				return names.toArray(new Long[0]);
			}

			if (ID_FOR.equals(operation)) {
				long objectName = (long) params[0];

				Object object = serverToolkit.getServerSession().objectFor(objectName);
				
				if (object == null) {
					// This will happen when a job tree is changin quicker on the server than
					// the client can keep up with.
					return null;
				}
				else {
					return directory.getIdFor(object);
				}
			}

			if (LOOKUP.equals(operation)) {

				String path = (String) params[0];

				Object object = directory.lookup(path);

				if (object == null) {
					return null;
				}
				
				long objectName = serverToolkit.getServerSession().nameFor(object);

				if (objectName >= 0) {
					return new Carrier(objectName);
				} else {
					return object;
				}
			}

			if (LOOKUP_TYPE.equals(operation)) {

				String path = (String) params[0];
				Class<?> type = (Class<?>) params[1];

				Object object;
				try {
					object = directory.lookup(path, type);
				} catch (ArooaConversionException e) {
					throw new MBeanException(e);
				}

				if (object == null) {
					return null;
				}
				
				long objectName = serverToolkit.getServerSession().nameFor(object);
				
				if (objectName >= 0) {
					return new Carrier(objectName);
				} else {
					return object;
				}
			}

			throw new ReflectionException(new IllegalStateException(
					"invoked for an unknown method."), operation.toString());
		}

		public void destroy() {
		}

	}

	static class Carrier implements Serializable {
		private static final long serialVersionUID = 2020062900L;

		private final long objectName;

		Carrier(long objectName) {
			this.objectName = objectName;
		}

		long getObjectName() {
			return objectName;
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
