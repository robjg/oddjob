package org.oddjob.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

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
import org.oddjob.Structural;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.BaseComponent;
import org.oddjob.images.IconHelper;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.ClientSessionImpl;
import org.oddjob.jmx.client.RemoteLogPoller;
import org.oddjob.jmx.client.ServerView;
import org.oddjob.jmx.client.SimpleNotificationProcessor;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogEnabled;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.logging.OddjobNDC;
import org.oddjob.persist.Persistable;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsExecutable;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ServiceState;
import org.oddjob.state.ServiceStateChanger;
import org.oddjob.state.ServiceStateHandler;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * @oddjob.description Connect to an Oddjob {@link org.oddjob.jmx.JMXServerJob}.
 * This job allows remote jobs to be monitored and controlled from 
 * a local Oddjob.
 * <p>
 * This job will run until it is manually stopped or until the remote server is
 * stopped. If this job is stopped it's state will be COMPLETE, if the server stops
 * this job's state will be INCOMPLETE.
 * <p>
 * To access and control jobs on a server from within a configuration file this
 * client job must have an id. If the client has an id of <code>'freds-pc'</code>
 * and the job on the server has an id of <code>'freds-job'</code>. The job on
 * the server can be accessed from the client using the expression
 * <code>${freds-pc/freds-job}</code>.
 * <p>
 * 
 * @oddjob.example
 * 
 * To create a connection to a remote server.
 * <pre>
 * &lt;jmx:client xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"
 *            id="freds-pc"
 *            name="Connection to Freds PC" 
 *            url="service:jmx:rmi:///jndi/rmi://pcfred/public-jobs"/&gt;
 * </pre>
 * 
 * @oddjob.example
 * 
 * Connect, run a remote job, and disconnect.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/ClientRunsServerJob.xml}
 * 
 * The run job starts the server job but doesn't wait for it to complete.
 * We would need to add a wait job for that.
 * 
 * @oddjob.example
 * 
 * Connect using a username and password to a secure server.
 * <pre>
 * &lt;jmx:client xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"
 *             url="service:jmx:rmi:///jndi/rmi://localhost/my-oddjob" &gt;
 *  &lt;environment&gt;
 *   &lt;jmx:client-credentials username="username"
 *                           password="password" /&gt;
 *  &lt;/environment&gt;
 * &lt;/jmx:client&gt;
 * </pre>
 * 
 * @oddjob.example
 * 
 * A local job triggers when a server job runs.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/ClientTrigger.xml}
 * 
 * @author Rob Gordon
 */

public class JMXClientJob extends BaseComponent
implements Runnable, Stateful, Resetable,
		Stoppable, Structural, 
		LogArchiver, ConsoleArchiver, LogEnabled,
		RemoteDirectoryOwner {
		
	public static final long DEFAULT_LOG_POLLING_INTERVAL = 5000;
	
	private final ServiceStateHandler stateHandler; 
	
	private final ServiceStateChanger stateChanger;
	
	private enum WhyStop {
		STOP_REQUEST,
		SERVER_STOPPED,
		HEARTBEAT_FAILURE
	}
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The JMX service URL.
	 * @oddjob.required No. If not provided the client connects to the Platform 
	 * MBean Server but this is really only useful for testing.
	 */
	private String url;

	/** 
	 * @oddjob.property
	 * @oddjob.description The heart beat interval, in milliseconds.
	 * @oddjob.required Not, defaults to 5 seconds.
	 */
	private long heartbeat = 5000;
	
	/** The log poller thread */
	private RemoteLogPoller logPoller;
	
	/** The notification processor thread */
	private SimpleNotificationProcessor notificationProcessor; 
	
	/** Child helper */
	private ChildHelper<Object> childHelper = new ChildHelper<Object>(this);
	
	/** The client session */
	private ClientSession clientSession;
		
	/** View of the main server bean. */
	private ServerView serverView;
	
	/** The connector */ 
	private JMXConnector cntor; 
	
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
		
		public void remove() throws JMException, IOException {
			mbsc.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), 
						this);
		}
	}
	
	private ServerStoppedListener serverStoppedListener;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The environment. Typically username/password
	 * credentials.
	 * @oddjob.required No.
	 */
	private Map<String, ?> environment;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The maximum number of log lines to retrieve for any
	 * component.
	 * @oddjob.required No.
	 */
	private int maxLoggerLines = LogArchiver.MAX_HISTORY;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The maximum number of console lines to retrieve for any
	 * component.
	 * @oddjob.required No.
	 */
	private int maxConsoleLines = LogArchiver.MAX_HISTORY;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The number of milliseconds between polling for new
	 * log events. Defaults to 5.
	 * @oddjob.required No.
	 */
	private long logPollingInterval = 5000;
	
	private static int instance;
	
	private Logger theLogger;

	public JMXClientJob() {
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
	protected ServiceStateHandler stateHandler() {
		return stateHandler;
	}
	
	protected ServiceStateChanger getStateChanger() {
		return stateChanger;
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
	
	protected Logger logger() {
		if (theLogger == null) {
			synchronized (JMXClientJob.class) {
				theLogger = Logger.getLogger(JMXClientJob.class.getName() 
						+ "." + String.valueOf(instance++));
			}
		}
		return theLogger;
	}
	
	public String loggerName() {
		return logger().getName();
	}
	
	/**
	 * Set naming service url.
	 * 
	 * @param url The name of the remote node in the naming service.
	 */
	public void setUrl(String lookup) {		
		this.url = lookup;
	}
	
	/**
	 * Get the JMX service URL.
	 * 
	 * @return The name of the remote node in the naming service.
	 */
	public String getUrl() {		
		return url;
	}

	/* (non-Javadoc)
	 * @see org.oddjob.logging.LogArchiver#addLogListener(org.oddjob.logging.LogListener, java.lang.String, org.oddjob.logging.LogLevel, long, long)
	 */
	public void addLogListener(LogListener l, Object component, LogLevel level,
			long last, int history) {
		stateHandler.assertAlive();
		
		if (logPoller == null) {
			throw new NullPointerException("logPoller not available");
		}
		logPoller.addLogListener(l, component, level, last, history);
		// force poller to poll.
		synchronized (logPoller) {
			logPoller.notifyAll();
		}
	}

	/* (non-Javadoc)
	 * @see org.oddjob.logging.LogArchiver#removeLogListener(org.oddjob.logging.LogListener)
	 */
	public void removeLogListener(LogListener l, Object component) {
		if (logPoller == null) {
			// must have been shut down.
			return;
		}
		logPoller.removeLogListener(l, component);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#addConsoleListener(org.oddjob.logging.LogListener, java.lang.Object, long, int)
	 */
	public void addConsoleListener(LogListener l, Object component, long last,
			int max) {
		stateHandler.assertAlive();
		
		if (logPoller == null) {
			throw new NullPointerException("logPoller not available");
		}
		logPoller.addConsoleListener(l, component, last, max);
		// force main thread to poll.
		synchronized (this) {
			notifyAll();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#removeConsoleListener(org.oddjob.logging.LogListener, java.lang.Object)
	 */
	public void removeConsoleListener(LogListener l, Object component) {
		if (logPoller == null) {
			// must have been shut down.
			return;
		}
		logPoller.removeConsoleListener(l, component);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.ConsoleArchiver#consoleIdFor(java.lang.Object)
	 */
	public String consoleIdFor(Object component) {
		return logPoller.consoleIdFor(component);
	}

	public void onInitialised() {
		if (maxConsoleLines == 0) {
			maxConsoleLines = LogArchiver.MAX_HISTORY;
		}
		if (maxLoggerLines == 0) {
			maxLoggerLines = LogArchiver.MAX_HISTORY;
		}
		if (logPollingInterval == 0) {
			logPollingInterval = DEFAULT_LOG_POLLING_INTERVAL;
		}
	}
	
	
	public void run() {
		OddjobNDC.push(logger().getName());
		try {
			if (!stateHandler.waitToWhen(new IsExecutable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ServiceState.STARTING);
					
				}
			})) {
				return;
			}
			logger().info("[" + JMXClientJob.this + "] Starting.");
			
			try {
				configure(JMXClientJob.this);
				
				onStart();
				
            	stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
            		public void run() {
            			getStateChanger().setState(ServiceState.STARTED);
            		}   
            	});				
			}
			catch (final Throwable e) {
				logger().warn("[" + JMXClientJob.this + "] Exception starting:", e);
				
				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					public void run() {
						getStateChanger().setStateException(e);
					}
				});
			}
		}
		finally {
			OddjobNDC.pop();
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void onStart() throws Exception {
		MBeanServerConnection mbsc;
		if (url == null) {
			logger().info("Connecting to the Platform MBean Server...");
			
			mbsc = ManagementFactory.getPlatformMBeanServer();
		}
		else {
			logger().info("Connecting to [" + url + "] ...");
			
			JMXServiceURL address = new JMXServiceURL(url);
			cntor = JMXConnectorFactory.connect(address, environment);
			mbsc = cntor.getMBeanServerConnection();
		}
		
		serverStoppedListener = new ServerStoppedListener(mbsc);
			
		notificationProcessor = new SimpleNotificationProcessor(logger());
		notificationProcessor.start();
		
		clientSession = new ClientSessionImpl(
				mbsc,
				notificationProcessor,
				getArooaSession(),
				logger());
		
		Object serverMain = clientSession.create(
				OddjobMBeanFactory.objectName(0));
		
		serverView = new ServerView(serverMain);
		
		notificationProcessor.enqueueDelayed(new Runnable() {
			public void run() {
				try {
					serverView.noop();
					notificationProcessor.enqueueDelayed(this, heartbeat);
				} catch (RuntimeException e) {
					try {
						doStop(WhyStop.HEARTBEAT_FAILURE, e);
					} catch (Exception e1) {
						logger().error("Failed to stop.", e1);
					}
				}
			}
			@Override
			public String toString() {
				return "Heartbeat";
			}
		}, heartbeat);
		
		
		this.logPoller = new RemoteLogPoller(serverMain, 
				maxConsoleLines, maxLoggerLines);
		
		serverView.startStructural(childHelper);

		logPoller.setLogPollingInterval(logPollingInterval);
		
		Thread t = new Thread(logPoller);
		t.start();
	}

	public void stop() {
		OddjobNDC.push(logger().getName());
		try {
			logger().debug("[" + this + "] Thread [" + 
					Thread.currentThread().getName() + "] requested  stop, " +
							"state is [" + lastStateEvent().getState() + "]");
			
			if (!stateHandler.waitToWhen(new IsStoppable(), 
					new Runnable() {
						public void run() {
						}
			})) {
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
			OddjobNDC.pop();
		}
	}
	
	private void doStop(final WhyStop why, final Exception cause) 
	throws JMException, IOException {

		logPoller.stop();
		
		// if not destroyed by remote peer
		if (why == WhyStop.STOP_REQUEST) {
			clientSession.destroy(serverView.getProxy());
		}		
		
		if (why != WhyStop.HEARTBEAT_FAILURE) {
			serverStoppedListener.remove();
			
			if (cntor != null) {
				cntor.close();
			}
		}
		
		childHelper.removeAllChildren();
		
		notificationProcessor.stopProcessor();
		
		serverStoppedListener = null;
		cntor = null;
		logPoller = null;
		notificationProcessor = null;
		
		stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				switch (why) {
				case HEARTBEAT_FAILURE:
					getStateChanger().setStateException(cause);
					logger().error("[" + JMXClientJob.this + 
							"] Stopped because of heartbeat Failure.", cause);
					break;
				case SERVER_STOPPED:
					getStateChanger().setState(ServiceState.INCOMPLETE);
					logger().info("[" + JMXClientJob.this + 
							"] Stopped because server Stopped.");
					break;
				default:
					getStateChanger().setState(ServiceState.COMPLETE);
					logger().info("[" + JMXClientJob.this + 
							"] Stopped.");
				}
			}
		});
	}
		
	public RemoteDirectory provideBeanDirectory() {
		if (serverView == null) {
			return null;
		}
		return serverView.provideBeanDirectory();
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.Structural#addStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.Structural#removeStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}
	
	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		return stateHandler.waitToWhen(new IsSoftResetable(), new Runnable() {
			public void run() {
				getStateChanger().setState(ServiceState.READY);

				logger().info("[" + JMXClientJob.this + "] Soft Reset." );
			}
		});
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		return stateHandler.waitToWhen(new IsHardResetable(), new Runnable() {
			public void run() {
				getStateChanger().setState(ServiceState.READY);

				logger().info("[" + JMXClientJob.this + "] Hard Reset." );
			}
		});
	}

	public int getMaxConsoleLines() {
		return maxConsoleLines;
	}
	public void setMaxConsoleLines(int maxConsoleLines) {
		this.maxConsoleLines = maxConsoleLines;
	}
	public int getMaxLoggerLines() {
		return maxLoggerLines;
	}
	public void setMaxLoggerLines(int maxLoggerLines) {
		this.maxLoggerLines = maxLoggerLines;
	}
	public long getLogPollingInterval() {
		return logPollingInterval;
	}
	public void setLogPollingInterval(long logPollingInterval) {
		this.logPollingInterval = logPollingInterval;
	}
	
	public String toString() {
	    if (name == null) {
	        return getClass().getSimpleName();
	    }
	    else {
	        return name;
	    }
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
	protected void onDestroy() {		
		super.onDestroy();
		
		stop();
	}
	
	/**
	 * Internal method to fire state.
	 */
	protected void fireDestroyedState() {
		
		if (!stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
			public void run() {
				stateHandler().setState(ServiceState.DESTROYED);
				stateHandler().fireEvent();
			}
		})) {
			throw new IllegalStateException("[" + JMXClientJob.this + " Failed set state DESTROYED");
		}
		logger().debug("[" + JMXClientJob.this + "] destroyed.");				
	}
}
