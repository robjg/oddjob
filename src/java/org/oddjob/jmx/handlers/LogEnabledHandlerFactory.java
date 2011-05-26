package org.oddjob.jmx.handlers;

import java.lang.reflect.UndeclaredThrowableException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.SimpleHandlerResolver;
import org.oddjob.jmx.server.JMXOperation;
import org.oddjob.jmx.server.JMXOperationFactory;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogHelper;

/**
 */
public class LogEnabledHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, LogEnabled> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	private static final JMXOperation<String> GET_LOGGER = 
		new JMXOperationFactory(LogEnabled.class
				).operationFor("loggerName", MBeanOperationInfo.INFO);
	
	public Class<Object> interfaceClass() {
		return Object.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
			GET_LOGGER.getOpInfo() };
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}	

	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit ojmb) {
		return new LogEnabledServerHandler(target, ojmb);
	}

	public ClientHandlerResolver<LogEnabled> clientHandlerFactory() {
		return new SimpleHandlerResolver<LogEnabled>(
				ClientLogPollableHandlerFactory.class.getName(),
				VERSION);
	}
	
	public static class ClientLogPollableHandlerFactory 
	implements ClientInterfaceHandlerFactory<LogEnabled> {
		
		public LogEnabled createClientHandler(LogEnabled ignored, ClientSideToolkit toolkit) {
			return new ClientLogEnabledHandler(toolkit);
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}
		
		public Class<LogEnabled> interfaceClass() {
			return LogEnabled.class;
		}
	}
	
	static class ClientLogEnabledHandler implements LogEnabled {
		
		/** Remember remote logger. */
		private final String remoteLoggerName;
		
		ClientLogEnabledHandler(ClientSideToolkit toolkit) {
			try {
				remoteLoggerName = toolkit.invoke(GET_LOGGER);
			} catch (Throwable t) {
				throw new UndeclaredThrowableException(t);
			}
		}
		
		public String loggerName() {
			return remoteLoggerName;
		}
	}
	

	
	class LogEnabledServerHandler implements ServerInterfaceHandler {
	
		private final String loggerName;
		
		LogEnabledServerHandler(Object object, ServerSideToolkit ojmb) {
			this.loggerName = LogHelper.getLogger(object);
		}
		
		public Object invoke(RemoteOperation<?> operation, Object[] params) 
		throws MBeanException, ReflectionException {

			if (GET_LOGGER.equals(operation)) {
				return loggerName;
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
}