package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOperation;
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
import java.util.Collections;
import java.util.List;

/**
 */
public class RunnableHandlerFactory 
implements ServerInterfaceHandlerFactory<Runnable, Runnable> {

	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	private static final JMXOperationPlus<Void> RUN =
			new JMXOperationPlus<>(
					"run",
					"Execute the job.",
					Void.TYPE,
					MBeanOperationInfo.ACTION);


	@Override
	public Class<Runnable> serverClass() {
		return Runnable.class;
	}

	@Override
	public Class<Runnable> clientClass() {
		return Runnable.class;
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
			RUN.getOpInfo() };
	}

	@Override
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Runnable target, ServerSideToolkit ojmb) {
		return new RunnableServerHandler(target, ojmb);
	}

	static class RunnableServerHandler implements ServerInterfaceHandler {
	
		private final Runnable runnable;
		private final ServerSideToolkit ojmb;
		
		RunnableServerHandler(Runnable runnable, ServerSideToolkit ojmb) {
			this.runnable = runnable;
			this.ojmb = ojmb;
		}

		@Override
		public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {
			if (RUN.equals(operation)) {
				
				ojmb.getContext().getModel().getThreadManager().run(
						runnable, "run invoked by client.");
				return null;
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