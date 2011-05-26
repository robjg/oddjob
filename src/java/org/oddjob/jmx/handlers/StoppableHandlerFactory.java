package org.oddjob.jmx.handlers;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.oddjob.Stoppable;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.VanillaHandlerResolver;
import org.oddjob.jmx.server.ServerAllOperationsHandler;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

/**
 * 
 */
public class StoppableHandlerFactory 
implements ServerInterfaceHandlerFactory<Stoppable, Stoppable> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	public Class<Stoppable> interfaceClass() {
		return Stoppable.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				new MBeanOperationInfo("stop", "Stop the job.",
						new MBeanParameterInfo[0], Void.TYPE.getName(),
						MBeanOperationInfo.ACTION)
			};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}
	
	public ServerInterfaceHandler createServerHandler(
			Stoppable target, ServerSideToolkit ojmb) {
		
		return new ServerAllOperationsHandler<Stoppable>(
				Stoppable.class, target);
	}

	public ClientHandlerResolver<Stoppable> clientHandlerFactory() {
		return new VanillaHandlerResolver<Stoppable>(
				Stoppable.class.getName());
	}
}