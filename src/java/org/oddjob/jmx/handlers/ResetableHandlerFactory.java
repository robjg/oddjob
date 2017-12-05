package org.oddjob.jmx.handlers;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.oddjob.Resetable;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.VanillaHandlerResolver;
import org.oddjob.jmx.server.ServerAllOperationsHandler;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

/**
 */
public class ResetableHandlerFactory 
implements ServerInterfaceHandlerFactory<Resetable, Resetable> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	public Class<Resetable> interfaceClass() {
		return Resetable.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				new MBeanOperationInfo("softReset",
						"Soft Reset the job.", new MBeanParameterInfo[0], Void.TYPE
								.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("hardReset",
						"Hard Reset the job.", new MBeanParameterInfo[0], Void.TYPE
								.getName(), MBeanOperationInfo.ACTION)
			};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}
	
	public ServerInterfaceHandler createServerHandler(Resetable target, ServerSideToolkit ojmb) {
		return new ServerAllOperationsHandler<Resetable>(
				Resetable.class, target);
	}

	public ClientHandlerResolver<Resetable> clientHandlerFactory() {
		return new VanillaHandlerResolver<Resetable>(
				Resetable.class.getName());
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