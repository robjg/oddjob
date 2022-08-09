package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.util.Collections;
import java.util.List;

/**
 * Handler for being a Remote Component which all remote components provide.
 */
public class RemoteOddjobHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, RemoteOddjobBean> {

	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);

	private static final JMXOperation<ServerInfo> SERVER_INFO = 
		new JMXOperationFactory(RemoteOddjobBean.class 
				).operationFor(
						"serverInfo", 
						"The OddjobMBean Server side information.",
						MBeanOperationInfo.INFO);
	
	private static final JMXOperation<Void> NOOP =
		new JMXOperationFactory(RemoteOddjobBean.class 
				).operationFor("noop", MBeanOperationInfo.INFO);

	@Override
	public Class<Object> serverClass() {
		return Object.class;
	}

	@Override
	public Class<RemoteOddjobBean> clientClass() {
		return RemoteOddjobBean.class;
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
			SERVER_INFO.getOpInfo(), 
			NOOP.getOpInfo() 
			};
	}

	@Override
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Object ignored, ServerSideToolkit toolkit) {
		return new RemoteOddjobServerHandler(toolkit.getRemoteBean());
	}

	static class RemoteOddjobServerHandler implements ServerInterfaceHandler {
	
		private final RemoteOddjobBean ojmb;
		
		RemoteOddjobServerHandler(RemoteOddjobBean ojmb) {
			this.ojmb = ojmb;
		}

		@Override
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