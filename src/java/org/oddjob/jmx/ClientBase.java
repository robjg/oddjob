package org.oddjob.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.BaseComponent;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.images.IconHelper;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.logging.LogEnabled;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ServiceState;
import org.oddjob.state.ServiceStateChanger;
import org.oddjob.state.ServiceStateHandler;

/**
 * Shared implementation for JMX clients.
 * 
 * @author rob
 *
 */
abstract public class ClientBase extends BaseComponent 
implements Runnable, Stateful, Resetable,
		Stoppable, LogEnabled {

	protected enum WhyStop {
		STOP_REQUEST,
		SERVER_STOPPED,
		HEARTBEAT_FAILURE
	}
	
	private static final AtomicInteger instanceCount = new AtomicInteger();
	
	private final Logger logger = Logger.getLogger(getClass().getName() + 
			"." + instanceCount.incrementAndGet());
	
	protected final ServiceStateHandler stateHandler; 
	
	private final ServiceStateChanger stateChanger;
	
	/** The notification processor. */
	private volatile ScheduledExecutorService notificationProcessor; 
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
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
	 * Constructor.
	 * 
	 */
	public ClientBase() {
		stateHandler = new ServiceStateHandler(this);
		stateChanger = new ServiceStateChanger(stateHandler, iconHelper, 
				new Persistable() {					
			@Override
			public void persist() throws ComponentPersistException {
				save();
			}
		});
	}
	
	@Override
	protected Logger logger() {
		return logger;
	}
	
	@Override
	public String loggerName() {
		return logger.getName();
	}
	
	@Override
	protected ServiceStateHandler stateHandler() {
		return stateHandler;
	}
	
	protected ServiceStateChanger getStateChanger() {
		return stateChanger;
	}
    
	
	public void run() {
		ComponentBoundry.push(logger().getName(), this);
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.STARTING);
					
				}
			})) {
				return;
			}
			logger().info("Starting.");
			
			try {
				configure(ClientBase.this);
				
				onStart();
				
            	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
            		public void run() {
            			getStateChanger().setState(ServiceState.STARTED);
            		}   
            	});				
			}
			catch (final Throwable e) {
				logger().warn("Exception starting:", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}
		}
		finally {
			ComponentBoundry.pop();
		}
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
	public void stop() {
		ComponentBoundry.push(logger().getName(), this);
		try {
			logger().debug("Stop requested.");
			
			if (!stateHandler.waitToWhen(new IsStoppable(), 
					new Runnable() {
						public void run() {
						}
			})) {
				logger().debug("Stop ignored - not running.");
				return;
			}
	
			iconHelper.changeIcon(IconHelper.STOPPING);
			
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					try {
						doStop(WhyStop.STOP_REQUEST, null);
					} 
					catch (Exception e) {
						iconHelper.changeIcon(IconHelper.EXECUTING);
						getStateChanger().setStateException(e);
					}
				}
			});
		}
		finally {
			ComponentBoundry.pop();
		}
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
				logger.debug("Failed to close connection: " + e);
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
	 * Perform a soft reset on the job.
	 */
	@Override
	public boolean softReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.READY);
	
					logger().info("Soft Reset complete." );
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	@Override
	public boolean hardReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.READY);
	
					logger().info("Hard Reset complete." );
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}

	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
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

	@Override
	public String toString() {
	    if (name == null) {
	        return getClass().getSimpleName();
	    }
	    else {
	        return name;
	    }
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
				logger.debug("Failed to remote MBeanServer NotificationListener: " +  
						e);
			} 
		}
	}
	
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		
		stop();
	}
	
	/**
	 * Internal method to fire state.
	 */
	@Override
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(ServiceState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + ClientBase.this + 
					"[ Failed set state DESTROYED");
		}
		logger().debug("[" + this + "] Destroyed.");				
	}

}
