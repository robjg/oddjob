package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOddjobBean;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.NoSuchOperationException;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.Collections;
import java.util.List;

/**
 * Handler for being a Remote Component which all remote components provide.
 */
public class RemoteOddjobHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, RemoteOddjobBean> {

	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);

	public static final JMXOperation<ServerInfo> SERVER_INFO =
		new JMXOperationFactory(RemoteOddjobBean.class 
				).operationFor(
						"serverInfo", 
						"The OddjobMBean Server side information.",
						MBeanOperationInfo.INFO);
	
	public static final JMXOperation<Void> NOOP =
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
		return new RemoteOddjobServerHandler(toolkit);
	}

	static class RemoteOddjobServerHandler implements ServerInterfaceHandler {
	
		private final ServerSideToolkit toolkit;
		
		RemoteOddjobServerHandler(ServerSideToolkit toolkit) {
			this.toolkit = toolkit;
		}

		@Override
		public Object invoke(RemoteOperation<?> operation, Object[] params) throws NoSuchOperationException {
			if (SERVER_INFO.equals(operation)) {
				return toolkit.getRemoteBean().serverInfo();
			}
			
			if (NOOP.equals(operation)) {
				toolkit.getRemoteBean().noop();
				return null;
			}

			throw NoSuchOperationException.of(toolkit.getRemoteId(),
					operation.getActionName(), operation.getSignature());
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