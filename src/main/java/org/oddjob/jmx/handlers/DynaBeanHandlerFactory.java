package org.oddjob.jmx.handlers;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.PropertyUtils;
import org.oddjob.framework.adapt.beanutil.WrapDynaClass;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

import javax.management.*;

/**
 */
public class DynaBeanHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, DynaBean> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	private static final JMXOperationPlus<Boolean> CONTAINS =
			new JMXOperationPlus<>(
					"contains",
					"Contains the mapped property.",
					Boolean.TYPE,
					MBeanOperationInfo.INFO
			).addParam(
					"property", String.class, "The property name.").addParam(
					"key", String.class, "The key.");
	
	private static final JMXOperationPlus<Object> GET_SIMPLE =
			new JMXOperationPlus<>(
					"get",
					"Get a simple property.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("property", String.class, "The property name.");
	
	private static final JMXOperationPlus<Object> GET_INDEXED =
			new JMXOperationPlus<>(
					"get",
					"Get an indexed property.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("property", String.class, "The property name.")
			.addParam("index", Integer.TYPE, "The index.");
									
	private static final JMXOperationPlus<Object> GET_MAPPED =
			new JMXOperationPlus<>(
					"get",
					"Get a mapped property.",
					Object.class,
					MBeanOperationInfo.INFO)
			.addParam("property", String.class, "The property name.")
			.addParam("key", String.class, "The key.");
	
	private static final JMXOperationPlus<DynaClass> GET_DYNACLASS =
			new JMXOperationPlus<>(
					"getDynaClass",
					"Get the DynaClass.",
					DynaClass.class,
					MBeanOperationInfo.INFO);
	
	private static final JMXOperationPlus<Void> REMOVE =
			new JMXOperationPlus<>(
					"remove",
					"Remove a mapped property.",
					Void.TYPE,
					MBeanOperationInfo.ACTION)
			.addParam("property", String.class, "The property name.")
			.addParam("key", String.class, "The key.");
	
	private static final JMXOperationPlus<Void> SET_SIMPLE =
			new JMXOperationPlus<>(
					"set",
					"Set a simple property.",
					Void.TYPE,
					MBeanOperationInfo.ACTION)
			.addParam("property", String.class, "The property name.")
			.addParam("value ", Object.class, "The value.");
	
	private static final JMXOperationPlus<Void> SET_INDEXED =
			new JMXOperationPlus<>(
					"set",
					"Set an indexed property.",
					Void.TYPE,
					MBeanOperationInfo.ACTION)
			.addParam("property", String.class, "The property name.")
			.addParam("index", Integer.TYPE, "The index.")
			.addParam("value ", Object.class, "The value.");
	
	private static final JMXOperationPlus<Void> SET_MAPPED =
			new JMXOperationPlus<>(
					"set",
					"Set a mapped property.",
					Void.TYPE,
					MBeanOperationInfo.ACTION)
			.addParam("property", String.class, "The property name.")
			.addParam("key", String.class, "The key.")
			.addParam("value ", Object.class, "The value.");


	@Override
	public Class<Object> serverClass() {
		return Object.class;
	}

	@Override
	public Class<DynaBean> clientClass() {
		return DynaBean.class;
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
				CONTAINS.getOpInfo(),		
				GET_SIMPLE.getOpInfo(),
				GET_INDEXED.getOpInfo(), 
				SET_MAPPED.getOpInfo(),
				GET_DYNACLASS.getOpInfo(),
				REMOVE.getOpInfo(),
				SET_SIMPLE.getOpInfo(),
				SET_INDEXED.getOpInfo(),
				SET_MAPPED.getOpInfo(),
			};
	}

	@Override
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Object target,
			ServerSideToolkit serverSideToolkit) {
		return new DynaBeanServerHandler(target);
	}

	static class DynaBeanServerHandler implements ServerInterfaceHandler {
	
		private final Object bean;
		
		DynaBeanServerHandler(Object bean) {
			this.bean = bean;
		}

		@Override
		public Object invoke(RemoteOperation<?> operation, Object[] params)
		throws MBeanException, ReflectionException {
			
			if (CONTAINS.equals(operation)) {
				try {
					return !(PropertyUtils.getMappedProperty(bean, (String) params[0],
									(String) params[1]) == null);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
			}		
			else if (GET_SIMPLE.equals(operation)) {
				String property = (String) params[0];
				try {
					return PropertyUtils.getProperty(bean, property);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
			}
			else if (GET_INDEXED.equals(operation)) {
				try {
					return PropertyUtils.getIndexedProperty(bean, (String) params[0],
							(Integer) params[1]);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
			}
			else if (GET_MAPPED.equals(operation)) {
				try {
					return PropertyUtils.getMappedProperty(bean, (String) params[0], 
							(String) params[1]);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
			}
			else if (GET_DYNACLASS.equals(operation)) {
				if (bean instanceof DynaBean) {
					return ((DynaBean) bean).getDynaClass();
				}
				else {
					return WrapDynaClass.createDynaClass(bean.getClass());
				}
			}
			else if (REMOVE.equals(operation)) {
				try {
					PropertyUtils.setMappedProperty(bean, (String) params[0], 
						(String) params[1], null);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
				return Void.TYPE;
			}
			else if (SET_INDEXED.equals(operation)) {
				try {
					PropertyUtils.setIndexedProperty(bean, (String) params[0],
							(Integer) params[1], params[2]);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
				return Void.TYPE;
			}
			else if (SET_SIMPLE.equals(operation)) {
				try {
					PropertyUtils.setProperty(bean, (String) params[0], 
							params[1]);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
				return Void.TYPE;
			}
			else if (SET_MAPPED.equals(operation)) {
				try {
					PropertyUtils.setMappedProperty(bean, (String) params[0], 
							(String) params[1], params[2]);
				} catch (Exception e) {
					throw new MBeanException(e);
				}
				return Void.TYPE;
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