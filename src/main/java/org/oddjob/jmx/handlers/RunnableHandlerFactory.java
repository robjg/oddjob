package org.oddjob.jmx.handlers;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.VanillaHandlerResolver;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

/**
 */
public class RunnableHandlerFactory 
implements ServerInterfaceHandlerFactory<Runnable, Runnable> {

	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	private static final JMXOperationPlus<Void> RUN = 
		new JMXOperationPlus<Void>(
				"run", 
				"Execute the job.",
				Void.TYPE,
				MBeanOperationInfo.ACTION);

	
	public Class<Runnable> interfaceClass() {
		return Runnable.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			RUN.getOpInfo() };
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}
	
	public ServerInterfaceHandler createServerHandler(Runnable target, ServerSideToolkit ojmb) {
		return new RunnableServerHandler(target, ojmb);
	}

	public ClientHandlerResolver<Runnable> clientHandlerFactory() {
		return new VanillaHandlerResolver<Runnable>(
				Runnable.class.getName());
	}
	
	class RunnableServerHandler implements ServerInterfaceHandler {
	
		private final Runnable runnable;
		private final ServerSideToolkit ojmb;
		
		RunnableServerHandler(Runnable runnable, ServerSideToolkit ojmb) {
			this.runnable = runnable;
			this.ojmb = ojmb;
		}
		
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