package org.oddjob.jmx.handlers;

import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.SharedConstants;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.LogPollable;
import org.oddjob.jmx.server.*;
import org.oddjob.logging.LogEvent;

import javax.management.*;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Provide Handlers for the {@link LogPollable} interface.
 * <p>
 * This is a special handler because url and consoleId never
 * change on the server so we can cache the values.
 * 
 * @author rob
 */
public class LogPollableHandlerFactory 
implements ServerInterfaceHandlerFactory<Object, LogPollable> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
	
	private static final JMXOperationPlus<String> CONSOLE_ID =
			new JMXOperationPlus<>(
					"consoleId",
					"Console ID",
					String.class,
					MBeanOperationInfo.INFO);
	
	private static final JMXOperationPlus<String> URL =
			new JMXOperationPlus<>(
					"url",
					"Remote URL",
					String.class,
					MBeanOperationInfo.INFO);
	
	private static final JMXOperationPlus<LogEvent[]> RETRIEVE_CONSOLE_EVENTS =
			new JMXOperationPlus<>(
					SharedConstants.RETRIEVE_CONSOLE_EVENTS_METHOD,
					"Retrieve Console Events",
					LogEvent[].class,
					MBeanOperationInfo.INFO)
			.addParam("seqNum", Long.TYPE, "Sequence Number")
			.addParam("history", Integer.TYPE, "History");
	
	private static final JMXOperationPlus<LogEvent[]> RETRIEVE_LOG_EVENTS =
			new JMXOperationPlus<>(
					SharedConstants.RETRIEVE_LOG_EVENTS_METHOD,
					"Retrieve Log Events",
					LogEvent[].class,
					MBeanOperationInfo.INFO)
			.addParam("seqNum", Long.TYPE, "Sequence Number")
			.addParam("history", Integer.TYPE, "History");

	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandlerFactory#interfaceClass()
	 */
	@Override
	public Class<Object> serverClass() {
		return Object.class;
	}

	@Override
	public Class<LogPollable> clientClass() {
		return LogPollable.class;
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
				CONSOLE_ID.getOpInfo(),
				URL.getOpInfo(),
				RETRIEVE_CONSOLE_EVENTS.getOpInfo(),
				RETRIEVE_LOG_EVENTS.getOpInfo()
		};
	}

	@Override
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {
		return new MBeanNotificationInfo[0];
	}

	@Override
	public ServerInterfaceHandler createServerHandler(Object target, ServerSideToolkit ojmb) {
		return new ServerLogPollableHandler(target, ojmb);
	}

	public static class ClientFactory
	implements ClientInterfaceHandlerFactory<LogPollable> {

		@Override
		public Class<LogPollable> interfaceClass() {
			return LogPollable.class;
		}

		@Override
		public HandlerVersion getVersion() {
			return VERSION;
		}

		@Override
		public LogPollable createClientHandler(LogPollable ignored, ClientSideToolkit toolkit) {
			return new ClientLogPollableHandler(toolkit);
		}
	}
	
	static class ClientLogPollableHandler implements LogPollable {

		private final String consoleId;
		private final String url;

		private final ClientSideToolkit toolkit;
		
		ClientLogPollableHandler(ClientSideToolkit toolkit) {

			this.toolkit = toolkit;
			
			try {
				consoleId = toolkit.invoke(
						CONSOLE_ID); 
				url = toolkit.invoke(
						URL);
			} catch (Throwable t) {
				throw new UndeclaredThrowableException(t);
			}
		}
		
		/**
		 * Get the consoleId which has been saved from the remote OddjobMBean. The console
		 * identifies the console on a remote server. The console will frequently be
		 * shared between components in a single JVM and so we don't want to get the same
		 * messages on a component by component bases.
		 *  
		 * @return The consoleId.
		 */
		@Override
		public String consoleId() {
			return consoleId;
		}

		@Override
		public String url() {
			return url;
		}

		@Override
		public LogEvent[] retrieveConsoleEvents(long from, int max) {
			try {
				return toolkit.invoke(
						RETRIEVE_CONSOLE_EVENTS,
						from, max);
			} catch (Throwable t) {
				throw new UndeclaredThrowableException(t);
			}
		}

		@Override
		public LogEvent[] retrieveLogEvents(long from, int max) {
			try {
				return toolkit.invoke(
						RETRIEVE_LOG_EVENTS,
						from, max);
			} catch (Throwable t) {
				throw new UndeclaredThrowableException(t);
			}
		}
	}
	
	static class ServerLogPollableHandler implements ServerInterfaceHandler {
	
		private final Object node;
		
		private final ServerContext srvcon;
		
		ServerLogPollableHandler(Object object, ServerSideToolkit ojmb) {
			this.node = object;
			this.srvcon = ojmb.getContext();
		}

		@Override
		public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {
			if (CONSOLE_ID.equals(operation)) {
				return LogArchiverHelper.consoleId(
						node, srvcon.getConsoleArchiver());
			}
			else if (URL.equals(operation))	{
				return srvcon.getServerId().toString();
			}
			else if (RETRIEVE_LOG_EVENTS.equals(operation)) {
				return LogArchiverHelper.retrieveLogEvents(node, 
						srvcon.getLogArchiver(), 
						(Long)params[0], (Integer)params[1]);
			}	
			else if (RETRIEVE_CONSOLE_EVENTS.equals(operation)) {
				return LogArchiverHelper.retrieveConsoleEvents(node, 
						srvcon.getConsoleArchiver(),
						(Long)params[0], (Integer)params[1]);
			}				
			else {
				throw new ReflectionException(
						new IllegalStateException("Invoked for an unknown method."), 
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