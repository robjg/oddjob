/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.registry.ServerId;
import org.oddjob.jmx.RemoteDirectory;
import org.oddjob.jmx.RemoteDirectoryOwner;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.SimpleHandlerResolver;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

public class BeanDirectoryHandlerFactory implements
		ServerInterfaceHandlerFactory<BeanDirectoryOwner, RemoteDirectoryOwner> {

	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	private static final JMXOperationPlus<ServerId> SERVER_ID = 
		new JMXOperationPlus<ServerId>(
				"serverId", 
				"Get ServerId.",
				ServerId.class,
				MBeanOperationInfo.INFO);

	private static final JMXOperationPlus<ObjectName[]> LIST = 
		new JMXOperationPlus<ObjectName[]>(
				"beansList", 
				"List Beans.",
				ObjectName[].class,
				MBeanOperationInfo.INFO);

	private static final JMXOperationPlus<String> ID_FOR = 
		new JMXOperationPlus<String>(
				"beansIdFor", 
				"Id For ObjectName.",
				String.class,
				MBeanOperationInfo.INFO)
			.addParam("objectName", ObjectName.class, "The object name.");

	private static final JMXOperationPlus<Object> LOOKUP = 
		new JMXOperationPlus<Object>(
				"beansLookup", 
				"Object lookup.",
				Object.class, 
				MBeanOperationInfo.INFO)
			.addParam("id", String.class, "The id.");

	private static final JMXOperationPlus<Object> LOOKUP_TYPE = 
		new JMXOperationPlus<Object>(
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
		return new SimpleHandlerResolver<RemoteDirectoryOwner>(
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
						Object result = toolkit.invoke(LOOKUP, new Object[] { path });
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

				public <T> T lookup(String path, Class<T> required)
						throws ArooaConversionException {
					try {
						Object result = toolkit.invoke(LOOKUP_TYPE, new Object[] {
								path, required });
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
					ObjectName objectName = toolkit.getClientSession().nameFor(bean);
		
					if (objectName == null) {
						return null;
					}
		
					try {
						return toolkit.invoke(ID_FOR,
								new Object[] { objectName });
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
		
				}
		
				public <T> Iterable<T> getAllByType(Class<T> type) {
					try {
						ObjectName[] names = toolkit.invoke(LIST,
								new Object[] { type });
						
						if (names == null) {
							return new ArrayList<T>();
						}
						
						List<T> results = new ArrayList<T>();
		
						for (ObjectName objectName : names) {
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
							this.serverId = toolkit.invoke(SERVER_ID,
									new Object[] {});
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
		ServerBeanDirectoryHandler handler = new ServerBeanDirectoryHandler(
				directory, serverToolkit);
		return handler;
	}

	class ServerBeanDirectoryHandler implements ServerInterfaceHandler {

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

				List<ObjectName> names = new ArrayList<ObjectName>();

				for (Object object : all) {
					ObjectName name = serverToolkit.getServerSession().nameFor(object);
					if (name != null) {
						names.add(name);
					}
				}

				return names.toArray(new ObjectName[0]);
			}

			if (ID_FOR.equals(operation)) {
				ObjectName objectName = (ObjectName) params[0];

				Object object = serverToolkit.getServerSession().objectFor(objectName);
				
				if (object == null) {
					// This will happen when a job tree is changin quicker on the server than
					// the client can keep up with.
					return null;
				}
				else {
					String id = directory.getIdFor(object);

					return id;
				}
			}

			if (LOOKUP.equals(operation)) {

				String path = (String) params[0];

				Object object = directory.lookup(path);

				if (object == null) {
					return null;
				}
				
				ObjectName objectName = serverToolkit.getServerSession().nameFor(object);

				if (objectName != null) {
					return new Carrier(objectName);
				} else {
					return object;
				}
			}

			if (LOOKUP_TYPE.equals(operation)) {

				String path = (String) params[0];
				Class<?> type = (Class<?>) params[1];

				Object object = null;
				try {
					object = directory.lookup(path, type);
				} catch (ArooaConversionException e) {
					throw new MBeanException(e);
				}

				if (object == null) {
					return null;
				}
				
				ObjectName objectName = serverToolkit.getServerSession().nameFor(object);
				
				if (objectName != null) {
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
		private static final long serialVersionUID = 2009061500L;

		private final ObjectName objectName;

		Carrier(ObjectName objectName) {
			this.objectName = objectName;
		}

		ObjectName getObjectName() {
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
