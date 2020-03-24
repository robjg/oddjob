package org.oddjob.jmx.handlers;

import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.oddjob.Describable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.describe.Describer;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.VanillaHandlerResolver;
import org.oddjob.jmx.server.JMXOperation;
import org.oddjob.jmx.server.JMXOperationFactory;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

/**
 * InterfaceHandler for the {@link org.oddjob.jmx.client.Describable}
 * interface.
 * 
 * @author Rob Gordon.
 */
public class DescribeableHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, Describable> {

	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	private static final JMXOperation<Map<String, String>> DESCRIBE = 
		new JMXOperationFactory(Describable.class
				).operationFor("describe", 
			"Describe properties.",
			MBeanOperationInfo.INFO);
	
	public Class<Object> interfaceClass() {
		return Object.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			DESCRIBE.getOpInfo()
			};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}
	
	
	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit ojmb) {
		return new ServerDescribeableHandler(target, 
				ojmb.getServerSession().getArooaSession());
	}

	public ClientHandlerResolver<Describable> clientHandlerFactory() {
		return new VanillaHandlerResolver<Describable>(
				Describable.class.getName());
	}
	
	class ServerDescribeableHandler implements ServerInterfaceHandler {
	
		private final Object object;
		private final Describer describer;
		
		ServerDescribeableHandler(Object object, ArooaSession session) {
			this.object = object;
			this.describer = new UniversalDescriber(session);
		}
		
		public Object invoke(RemoteOperation<?> operation, Object[] params) 
		throws MBeanException, ReflectionException {

			if (DESCRIBE.equals(operation)) {
				return describer.describe(object);
			}
			else {
				throw new ReflectionException(
						new IllegalStateException("invoked for an unknown method."), 
								operation.getActionName());				
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