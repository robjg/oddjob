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
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeanDirectoryHandlerFactory implements
		ServerInterfaceHandlerFactory<BeanDirectoryOwner, RemoteDirectoryOwner> {

	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	public static final JMXOperationPlus<ServerId> SERVER_ID =
			new JMXOperationPlus<>(
					"serverId",
					"Get ServerId.",
					ServerId.class,
					MBeanOperationInfo.INFO);

	public static final JMXOperationPlus<long[]> LIST =
		new JMXOperationPlus<>(
				"beansList", 
				"List Beans.",
				long[].class,
				MBeanOperationInfo.INFO)
			.addParam("type", Class.class, "The type required");

	public static final JMXOperationPlus<String> ID_FOR =
			new JMXOperationPlus<>(
					"beansIdFor",
					"Id For ObjectName.",
					String.class,
					MBeanOperationInfo.INFO)
			.addParam("remoteId", long.class, "The object name.");

	public static final JMXOperationPlus<Object> LOOKUP =
			new JMXOperationPlus<>(
					"beansLookup",
					"Object lookup.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("id", String.class, "The id.");

	public static final JMXOperationPlus<Object> LOOKUP_TYPE =
			new JMXOperationPlus<>(
					"beansLookupType",
					"Object lookup.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("id", String.class, "The id.")
			.addParam("type", Class.class, "The type.");

	@Override
	public Class<BeanDirectoryOwner> serverClass() {
		return BeanDirectoryOwner.class;
	}

	@Override
	public Class<RemoteDirectoryOwner> clientClass() {
		return RemoteDirectoryOwner.class;
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
		return new MBeanOperationInfo[] { SERVER_ID.getOpInfo(),
				LIST.getOpInfo(), ID_FOR.getOpInfo(), LOOKUP.getOpInfo(),
				LOOKUP_TYPE.getOpInfo() };
	}

	@Override
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	public static class ClientFactory implements
			ClientInterfaceHandlerFactory<RemoteDirectoryOwner> {

		@Override
		public Class<RemoteDirectoryOwner> interfaceClass() {
			return RemoteDirectoryOwner.class;
		}

		@Override
		public HandlerVersion getVersion() {
			return VERSION;
		}

		@Override
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

		@Override
		public RemoteDirectory provideBeanDirectory() {
			return new RemoteDirectory() {

				private ServerId serverId;

				@Override
				public Object lookup(String path) {
					try {
						Object result = toolkit.invoke(LOOKUP, path);
						if (result instanceof Carrier) {
							return toolkit.getClientSession().create(
									((Carrier) result).getRemoteId());
						} else {
							return result;
						}
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}

				@Override
				public <T> T lookup(String path, Class<T> required) {
					try {
						Object result = toolkit.invoke(LOOKUP_TYPE, path, required);
						if (result instanceof Carrier) {
							result = toolkit.getClientSession().create(
									((Carrier) result).getRemoteId());
						}
						return required.cast(result);
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
				}

				@Override
				public String getIdFor(Object bean) {
					long objectName = toolkit.getClientSession().idFor(bean);
		
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

				@Override
				public <T> Iterable<T> getAllByType(Class<T> type) {
					try {
						long[] names = toolkit.invoke(LIST, type);
						
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

				@Override
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

	@Override
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

		@Override
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
					long name = serverToolkit.getServerSession().idFor(object);
					if (name >= 0) {
						names.add(name);
					}
				}

				return names.stream().mapToLong(Long::longValue).toArray();
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
				
				long objectName = serverToolkit.getServerSession().idFor(object);

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
				
				long objectName = serverToolkit.getServerSession().idFor(object);
				
				if (objectName >= 0) {
					return new Carrier(objectName);
				} else {
					return object;
				}
			}

			throw new ReflectionException(new IllegalStateException(
					"invoked for an unknown method."), operation.toString());
		}

		@Override
		public void destroy() {
		}

	}

	public static class Carrier implements Serializable {
		private static final long serialVersionUID = 2020062900L;

		private final long remoteId;

		public Carrier(long remoteId) {
			this.remoteId = remoteId;
		}

		public long getRemoteId() {
			return remoteId;
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
