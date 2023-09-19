package org.oddjob.jmx.handlers;

import org.oddjob.Resettable;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.ServerAllOperationsHandler;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.remote.NotificationType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.Collections;
import java.util.List;

/**
 */
public class ResettableHandlerFactory
implements ServerInterfaceHandlerFactory<Resettable, Resettable> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);

	@Override
	public Class<Resettable> serverClass() {
		return Resettable.class;
	}

	@Override
	public Class<Resettable> clientClass() {
		return Resettable.class;
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
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Resettable target, ServerSideToolkit toolkit) {
		return new ServerAllOperationsHandler<>(
				Resettable.class, target, toolkit.getRemoteId());
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