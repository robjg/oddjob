package org.oddjob.jmx;

import javax.management.MBeanServerConnection;

import org.oddjob.Structural;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.ClientSessionImpl;
import org.oddjob.jmx.client.RemoteLogPoller;
import org.oddjob.jmx.client.ServerView;
import org.oddjob.jmx.client.SimpleNotificationProcessor;
import org.oddjob.jmx.server.OddjobMBeanFactory;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
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
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/ClientExample.xml}
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
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/SecureClientExample.xml}
 * 
 * @oddjob.example
 * 
 * A local job triggers when a server job runs.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/ClientTrigger.xml}
 * 
 * 
 * @author Rob Gordon
 */
public class JMXClientJob extends ClientBase
implements Structural, LogArchiver, ConsoleArchiver, RemoteDirectoryOwner {
		
	public static final long DEFAULT_LOG_POLLING_INTERVAL = 5000;
	
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
	
	/** 
	 * @oddjob.property url
	 * @oddjob.description This property is now deprecated in favour of 
	 * connection which reflects that the connection string no longer need 
	 * not only be a full JMX URL. 
	 * @oddjob.required No.
	 */
	@Deprecated
	public void setUrl(String url) {
		setConnection(url);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.logging.LogArchiver#addLogListener(org.oddjob.logging.LogListener, java.lang.String, org.oddjob.logging.LogLevel, long, long)
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public String consoleIdFor(Object component) {
		return logPoller.consoleIdFor(component);
	}

	@Override
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
	
	/**
	 * 
	 * @throws Exception
	 */
	@Override
	protected void doStart(MBeanServerConnection mbsc) throws Exception {
			
		notificationProcessor = new SimpleNotificationProcessor(logger());
		notificationProcessor.start();
		
		clientSession = new ClientSessionImpl(
				mbsc,
				notificationProcessor,
				getArooaSession(),
				logger());
		
		Object serverMain = clientSession.create(
				OddjobMBeanFactory.objectName(0));
		
		if (serverMain == null) {
			throw new NullPointerException("No Oddjob MBean found.");
		}
		
		serverView = new ServerView(serverMain);
				
		this.logPoller = new RemoteLogPoller(serverMain, 
				maxConsoleLines, maxLoggerLines);
		
		serverView.startStructural(childHelper);

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
		
		logPoller.setLogPollingInterval(logPollingInterval);
		
		Thread t = new Thread(logPoller);
		t.start();
	}

	
	@Override
	protected void onStop(final WhyStop why) {

		logPoller.stop();
		
		// if not destroyed by remote peer
		if (why == WhyStop.STOP_REQUEST) {
			clientSession.destroy(serverView.getProxy());
		}		
		
		childHelper.removeAllChildren();
		
		notificationProcessor.stopProcessor();
		
		clientSession.destroyAll();
		
		logPoller = null;
		notificationProcessor = null;
		
	}
		
	@Override
	public RemoteDirectory provideBeanDirectory() {
		if (serverView == null) {
			return null;
		}
		return serverView.provideBeanDirectory();
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.Structural#addStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	@Override
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.oddjob.Structural#removeStructuralListener(org.oddjob.structural.StructuralListener)
	 */
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
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
	
	public long getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}

}
