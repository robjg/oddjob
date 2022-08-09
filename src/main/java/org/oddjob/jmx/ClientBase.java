package org.oddjob.jmx;

import org.oddjob.framework.extend.SimpleService;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.ServiceState;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Shared implementation for JMX clients.
 * 
 * @author rob
 *
 */
abstract public class ClientBase extends SimpleService {

	protected enum WhyStop {
		STOP_REQUEST,
		SERVER_STOPPED,
		HEARTBEAT_FAILURE
	}
	
	/** The notification processor. */
	private volatile ScheduledExecutorService notificationProcessor; 
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The JMX service URL. This is can be either
	 * the full blown convoluted JMX Service URL starting 
	 * <code>service.jmx....</code> or it can just be the last part of the
	 * form <code>hostname[:port][/instance-name]</code>.
	 * @oddjob.required No. If not provided the client connects to the Platform 
	 * MBean Server for the current VM.
	 */
	private String connection;

	/** The connector */ 
	private volatile JMXConnector cntor; 
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The heart beat interval, in milliseconds.
	 * @oddjob.required Not, defaults to 5 seconds.
	 */
	private long heartbeat = 5000;
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The environment. Typically username/password
	 * credentials.
	 * @oddjob.required No.
	 */
	private Map<String, ?> environment;
	
	/**
	 * Construct a new instance.
	 * 
	 */
	public ClientBase() {
	}	
	
	/**
	 * 
	 * @throws Exception
	 */
	protected void onStart() throws Exception {
		MBeanServerConnection mbsc;
		if (connection == null) {
			logger().info("Connecting to the Platform MBean Server...");
			
			mbsc = ManagementFactory.getPlatformMBeanServer();
		}
		else {
			logger().info("Connecting to [" + connection + "] ...");
			
			JMXServiceURL address = new JMXServiceURLHelper().parse(connection);
			cntor = JMXConnectorFactory.connect(address, environment);
			mbsc = cntor.getMBeanServerConnection();
			
			cntor.addConnectionNotificationListener(
					new ServerStoppedListener(), null, null);
		}
		
		notificationProcessor = Executors.newSingleThreadScheduledExecutor();
		
		doStart(mbsc, notificationProcessor);
	}
	
	/**
	 * Overridden by subclasses to provide a specific startup. 
	 * 
	 * @param mbsc
	 * @param notificationProcessor
	 * @throws Exception
	 */
	abstract protected void doStart(MBeanServerConnection mbsc, 
			ScheduledExecutorService notificationProcessor) 
	throws Exception;
		
	@Override
	protected void onStop() {
		doStop(WhyStop.STOP_REQUEST, null);
	}
	
	protected void doStop(final WhyStop why, final Exception cause) {

		// There is a small possibility that the SERVER_STOPPED and 
		// HEARTBEAT_FAILURE happen simultaneously.
		ExecutorService notificationProcessor;
		synchronized (this) {
			notificationProcessor = this.notificationProcessor;
			if (notificationProcessor == null) {
				return;
			}
			this.notificationProcessor = null;
		}
		notificationProcessor.shutdownNow();
		
		onStop(why);
		
		if (why == WhyStop.STOP_REQUEST && cntor != null) {
			try {
				cntor.close();
			} catch (IOException e) {
				logger().debug("Failed to close connection: " + e);
			}
		}
		cntor = null;
		
		stateHandler().waitToWhen(new IsAnyState(), () -> {
			switch (why) {
			case HEARTBEAT_FAILURE:
				getStateChanger().setStateException(cause);
				logger().error(
						"Client stopped because of heartbeat Failure.",
						cause);
				break;
			case SERVER_STOPPED:
				getStateChanger().setStateException(
						new Exception("Server Stopped."));
				logger().info("Client stopped because server Stopped.");
				break;
			default:
				getStateChanger().setState(ServiceState.STOPPED);
				logger().debug(
						"Client stopped because stop was requested.");
			}
		});
	}
	
	abstract protected void onStop(WhyStop why);
	
	/**
	 * Set naming service url.
	 * 
	 * @param connection The name of the remote node in the naming service.
	 */
	public void setConnection(String connection) {
		this.connection = connection;
	}
	
	/**
	 * Get the JMX service URL.
	 * 
	 * @return The name of the remote node in the naming service.
	 */
	public String getConnection() {		
		return connection;
	}

	public Map<String, ?> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, ?> environment) {
		this.environment = environment;
	}

	public long getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}

	/**
	 * Listener to detect when server has stopped.
	 */
	class ServerStoppedListener implements NotificationListener {
	
		@Override
		public void handleNotification(Notification notification,
				Object handback) {
			String notificationType = notification.getType();
			logger().debug("Connection Notification Listener recevied: " +
					notificationType);
			
			if (JMXConnectionNotification.CLOSED.equals(notificationType)) {
				try {
					doStop(WhyStop.SERVER_STOPPED, null);
				} catch (Exception e1) {
					logger().error(
							"Failed to stop from Connection Notification Listener:", 
							e1);
				}
			}
			
		}
	}
	
}
