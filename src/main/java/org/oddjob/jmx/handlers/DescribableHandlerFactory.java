package org.oddjob.jmx.handlers;

import org.oddjob.Describable;
import org.oddjob.describe.Describer;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.NoSuchOperationException;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * InterfaceHandler for the {@link Describable} interface.
 * 
 * @author Rob Gordon.
 */
public class DescribableHandlerFactory
implements ServerInterfaceHandlerFactory<Object, Describable> {

	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	private static final JMXOperation<Map<String, String>> DESCRIBE = 
		new JMXOperationFactory(Describable.class
				).operationFor("describe", 
			"Describe properties.",
			MBeanOperationInfo.INFO);

	@Override
	public Class<Object> serverClass() {
		return Object.class;
	}

	@Override
	public Class<Describable> clientClass() {
		return Describable.class;
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
			DESCRIBE.getOpInfo()
			};
	}

	@Override
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit toolkit) {
		return new ServerDescribableHandler(target, toolkit);
	}

	static class ServerDescribableHandler implements ServerInterfaceHandler {

		private final Object object;
		private final Describer describer;

		private final ServerSideToolkit toolkit;
		ServerDescribableHandler(Object object, ServerSideToolkit toolkit) {
			this.object = object;
			this.toolkit = toolkit;
			this.describer = new UniversalDescriber(toolkit.getServerSession().getArooaSession());
		}

		@Override
		public Object invoke(RemoteOperation<?> operation, Object[] params) throws NoSuchOperationException {

			if (DESCRIBE.equals(operation)) {
				return describer.describe(object);
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