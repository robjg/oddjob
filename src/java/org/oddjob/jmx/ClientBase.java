package org.oddjob.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.oddjob.FailedToStopException;
import org.oddjob.framework.SimpleService;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ServiceState;

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
	
	/** Listener for why the server stopped. */
	private volatile ServerStoppedListener serverStoppedListener;
	
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
		}
		
		serverStoppedListener = new ServerStoppedListener(mbsc);
			
		notificationProcessor = Executors.newSingleThreadScheduledExecutor();
		
		doStart(mbsc, notificationProcessor);
	}
	
	abstract protected void doStart(MBeanServerConnection mbsc, 
			ScheduledExecutorService notificationProcessor) 
	throws Exception;
		
	@Override
	protected void onStop() throws FailedToStopException {
		doStop(WhyStop.STOP_REQUEST, null);
	}
	
	protected void doStop(final WhyStop why, final Exception cause) {

		// There is a small possibility that the SERVER_STOPPED and 
		// HEARTBEAT_FAIURE happen simultaneously. 
		ServerStoppedListener serverStoppedListener;
		synchronized (this) {
			serverStoppedListener = this.serverStoppedListener;
			if (serverStoppedListener == null) {
				return;
			}
			this.serverStoppedListener = null;
		}
		
		if (why == WhyStop.STOP_REQUEST ) {
			serverStoppedListener.remove();
		}
		
		onStop(why);
		
		notificationProcessor.shutdownNow();
		notificationProcessor = null;

		if (why ==  WhyStop.STOP_REQUEST && cntor != null) {
			try {
				cntor.close();
			} catch (IOException e) {
				logger().debug("Failed to close connection: " + e);
			}
		}
		cntor = null;
		
		stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				switch (why) {
				case HEARTBEAT_FAILURE:
					getStateChanger().setStateException(cause);
					logger().error("Stopped because of heartbeat Failure.", cause);
					break;
				case SERVER_STOPPED:
					getStateChanger().setStateException(
							new Exception("Server Stopped"));
					logger().info("Stopped because server Stopped.");
					break;
				default:
					getStateChanger().setState(ServiceState.COMPLETE);
					logger().info("Stopped.");
				}
			}
		});
	}
	
	abstract protected void onStop(WhyStop why);
	
	/**
	 * Set naming service url.
	 * 
	 * @param connection The name of the remote node in the naming service.
	 */
	public void setConnection(String lookup) {		
		this.connection = lookup;
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

	private class ServerStoppedListener implements NotificationListener {
		
		private final MBeanServerConnection mbsc;
 
		public ServerStoppedListener(MBeanServerConnection mbsc) throws JMException, IOException {
			this.mbsc = mbsc;
			
			MBeanServerNotificationFilter serverFilter = new MBeanServerNotificationFilter();
			serverFilter.disableAllObjectNames();
			serverFilter.enableObjectName(OddjobMBeanFactory.objectName(0));
			mbsc.addNotificationListener(
					new ObjectName("JMImplementation:type=MBeanServerDelegate"),
					this, serverFilter, null);
			
		}
		
		public void handleNotification(Notification notification, Object handback) {
			if ("JMX.mbean.unregistered".equals(notification.getType())) {
				logger().debug("MBeanServerDelgate unregestered in server. Server has stopped.");
				try {
					doStop(WhyStop.SERVER_STOPPED, null);
				} catch (Exception e1) {
					logger().error("Failed to stop.", e1);
				}
				
			}
		}
		
		public void remove() {
			try {
				mbsc.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), 
							this);
			} catch (Exception e) {
				logger().debug("Failed to remote MBeanServer NotificationListener: " +  
						e);
			} 
		}
	}
	
}
