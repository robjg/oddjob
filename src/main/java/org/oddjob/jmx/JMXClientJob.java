package org.oddjob.jmx;

import org.oddjob.Structural;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.ClientSessionImpl;
import org.oddjob.jmx.client.RemoteLogPoller;
import org.oddjob.jmx.client.ServerView;
import org.oddjob.jobs.job.StopJob;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogListener;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

import javax.management.MBeanServerConnection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @oddjob.description Connect to an Oddjob {@link org.oddjob.jmx.JMXServerJob}.
 * This job allows remote jobs to be monitored and controlled from 
 * a local Oddjob.
 * <p>
 * This service will run until it is manually stopped or until the connection
 * to the remote server is lost. If this job is stopped it's state will be 
 * COMPLETE, if the connection is lost the state state will be EXCEPTION.
 * <p>
 * To access and control jobs on a server from within a configuration file this
 * client must have an id. If the client has an id of <code>'freds-pc'</code>
 * and the job on the server has an id of <code>'freds-job'</code>. The job on
 * the server can be accessed from the client using the expression
 * <code>${freds-pc/freds-job}</code>.
 * <p>
 * 
 * @oddjob.example
 * 
 * Connect to a remote server that is using the Platform MBean Server. This
 * example also demonstrates using the value of a remote jobs property.
 * 
 * {@oddjob.xml.resource org/oddjob/jmx/PlatformMBeanClientExample.xml}
 * 
 * Note that the {@link StopJob} is required otherwise Oddjob wouldn't exit. An 
 * Alternative to using stop, would be to make the client a child of a
 * {@link org.oddjob.jobs.structural.SequentialJob} with an
 * {@link org.oddjob.state.ServiceManagerStateOp} operator.
 * <p>
 * Here's an example of the command used to launch it:
 * <pre>
 * java -jar C:\Users\rob\projects\oddjob\run-oddjob.jar -f C:\Users\rob\projects\oddjob\test\java\org\oddjob\jmx\PlatformMBeanClientExample.xml localhost:13013
 * </pre>
 * 
 * This configuration is the client side of the first example in 
 * {@link JMXServerJob}.
 * 
 * @oddjob.example
 * 
 * To create a connection to a remote server that is using an RMI registry
 * using the full form of the JMX URL.
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
	
	/** The log poller thread */
	private RemoteLogPoller logPoller;
	
	/** Child helper */
	private ChildHelper<Object> childHelper = new ChildHelper<>(this);
	
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
	 * only be a full JMX URL. 
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
		stateHandler().assertAlive();
		
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
		stateHandler().assertAlive();
		
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
	 */
	@Override
	protected void doStart(MBeanServerConnection mbsc,
			ScheduledExecutorService notificationProcessor) {
			
		clientSession = new ClientSessionImpl(
				mbsc,
				notificationProcessor,
				getArooaSession(),
				logger());
		
		Object serverMain = clientSession.create(0L);
		
		if (serverMain == null) {
			throw new NullPointerException("No Oddjob MBean found.");
		}
		
		serverView = new ServerView(serverMain);
				
		this.logPoller = new RemoteLogPoller(serverMain, 
				maxConsoleLines, maxLoggerLines);
		
		serverView.startStructural(childHelper);

		notificationProcessor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					serverView.noop();
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
		}, getHeartbeat(), getHeartbeat(), TimeUnit.MILLISECONDS);
		
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
		
		clientSession.destroyAll();
		
		logPoller = null;
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
}
