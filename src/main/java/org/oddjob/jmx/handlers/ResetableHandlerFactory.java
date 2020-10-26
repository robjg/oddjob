package org.oddjob.jmx.handlers;

import org.oddjob.Resetable;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.ServerAllOperationsHandler;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 */
public class ResetableHandlerFactory 
implements ServerInterfaceHandlerFactory<Resetable, Resetable> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);

	@Override
	public Class<Resetable> serverClass() {
		return Resetable.class;
	}

	@Override
	public Class<Resetable> clientClass() {
		return Resetable.class;
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
				new MBeanOperationInfo("softReset",
						"Soft Reset the job.", new MBeanParameterInfo[0], Void.TYPE
								.getName(), MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("hardReset",
						"Hard Reset the job.", new MBeanParameterInfo[0], Void.TYPE
								.getName(), MBeanOperationInfo.ACTION)
			};
	}

	@Override
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Resetable target, ServerSideToolkit ojmb) {
		return new ServerAllOperationsHandler<>(
				Resetable.class, target);
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