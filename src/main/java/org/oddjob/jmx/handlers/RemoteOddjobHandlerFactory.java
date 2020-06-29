package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.VanillaHandlerResolver;
import org.oddjob.jmx.server.*;

import javax.management.*;

/**
 */
public class RemoteOddjobHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, RemoteOddjobBean> {

	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);

	private static final JMXOperation<ServerInfo> SERVER_INFO = 
		new JMXOperationFactory(RemoteOddjobBean.class 
				).operationFor(
						"serverInfo", 
						"The OddjobMBean Server side information.",
						MBeanOperationInfo.INFO);
	
	private static final JMXOperation<Void> NOOP =
		new JMXOperationFactory(RemoteOddjobBean.class 
				).operationFor("noop", MBeanOperationInfo.INFO);

	public Class<Object> interfaceClass() {
		return Object.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			SERVER_INFO.getOpInfo(), 
			NOOP.getOpInfo() 
			};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}
	
	public ServerInterfaceHandler createServerHandler(Object ignored, ServerSideToolkit toolkit) {
		return new RemoteOddjobServerHandler(toolkit.getRemoteBean());
	}

	public ClientHandlerResolver<RemoteOddjobBean> clientHandlerFactory() {
		return new VanillaHandlerResolver<>(
				RemoteOddjobBean.class.getName());
	}
	
	static class RemoteOddjobServerHandler implements ServerInterfaceHandler {
	
		private final RemoteOddjobBean ojmb;
		
		RemoteOddjobServerHandler(RemoteOddjobBean ojmb) {
			this.ojmb = ojmb;
		}
		
		public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {
			if (SERVER_INFO.equals(operation)) {
				return ojmb.serverInfo();
			}
			
			if (NOOP.equals(operation)) {
				ojmb.noop();
				return null;
			}

			throw new ReflectionException(
						new IllegalStateException("invoked for an unknown method."), 
								operation.toString());				
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