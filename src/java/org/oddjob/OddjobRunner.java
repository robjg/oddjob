package org.oddjob;

import org.apache.log4j.Logger;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

/**
 * A Wrapper for running Oddjob that ensures a smooth shutdown.
 * 
 * @author rob
 *
 */
public class OddjobRunner {
	private static final Logger logger = Logger.getLogger(OddjobRunner.class);
		
	public static final String KILLER_TIMEOUT_PROPERTY = 
		"oddjob.shutdown.killer.timeout";
	
	public static final long DEFAULT_KILLER_TIMEOUT = 15000L;
	
	/** The Oddjob we're running. */
	private final Oddjob oddjob;
	
	/** Flag if Oddjob is being destroyed from the Shutdown Hook. */
	private volatile boolean destroying = false;

	/** The killer thread time out. */
	private final long killerTimeout;
	
	/**
	 * Constructor.
	 * 
	 * @param oddjob The Oddjob to run.
	 */
	public OddjobRunner(Oddjob oddjob) {
		this.oddjob = oddjob;
		
		String timeoutProperty = System.getProperty(KILLER_TIMEOUT_PROPERTY);
		if (timeoutProperty == null) {
			killerTimeout = DEFAULT_KILLER_TIMEOUT;
		}
		else {
			killerTimeout = Long.parseLong(timeoutProperty);
		}
	}
	
	public Oddjob getOddjob() {
		return oddjob;
	}
	
	public void run() {
		
		logger.info("Starting Oddjob version " + oddjob.getVersion());
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());		
		try {
			oddjob.run();
			// This needs to be thought out a bit more.
			// The logic goes along the lines of if the main
			// thread get's here we need to wait until the Oddjob
			// has completed and then we can destroy it. 
			// mainly to close down executors, so the application
			// can terminate.
			if (destroying) {
				logger.debug("Oddjob execution thread complete via destroy from the shutdown hook.");
			}
			else {
				logger.debug("Oddjob execution thread completed. " +
						"Executers will be terminated when asynchronous jobs complete.");
				
			    oddjob.addStateListener(new StateListener() {
			    	public void jobStateChange(StateEvent event) {
			    		if (!new IsStoppable().test(event.getState())) {
			    				oddjob.removeStateListener(this);
			    				oddjob.stopExecutors();
			    		}
			    	}
			    });
			}
			
		} catch (Throwable t) {
			logger.fatal("Exception running Oddjob.", t);
			// uses halt, not exit as we don't want to invoke the 
			// shutdown hook
			Runtime.getRuntime().halt(1);
		}
	}
	
	/**
	 * Oddjob's shutdown hook.
	 *
	 */
	class ShutdownHook extends Thread {
		
		/** Killer thread will forcibly halt Oddjob if it hasn't terminated
		 * cleanly. */
		private Thread killer;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {

			logger.info("Shutdown Hook Executing.");
			
			setDaemon(true);
			
			// killer will just kill process if we can't stop in 15 sec
			killer = new Thread(new Runnable() {
				public void run() {
					logger.debug("Killer thread started. Oddjob has " + 
								killerTimeout + "ms to stop niceley.");
					try {
						Thread.sleep(killerTimeout);
					}
					catch (InterruptedException e) {
						logger.debug("Killer thread interrupted and terminating.");
						return;
					}
					logger.error("Failed to stop Oddjob nicely, using halt(-1)");
					Runtime.getRuntime().halt(-1);
				}
			});
			
			// start the killer. Not sure it really need to be daemon but
			// it does no harm.
			logger.debug("Starting killer thread.");
			killer.setDaemon(true);
			killer.start();

			StateEvent lastStateEvent = oddjob.lastStateEvent();

			if (lastStateEvent.getState().isStoppable()) {
				// Try stopping Oddjob. Do this even though destroy will also
				// stop Oddjob, so we can capture the stopped state.
				try {
					oddjob.stop();
					lastStateEvent = oddjob.lastStateEvent();
				} catch (FailedToStopException e) {
					logger.error("Failed to stop Oddjob", e);
					lastStateEvent = new StateEvent(
							oddjob, ParentState.EXCEPTION, e);
				}
			}
			
			logger.debug("Destroying Oddjob.");
			destroying = true;
			oddjob.destroy();
			
			// Nothing's hanging so we don't need our killer.
			killer.interrupt();
			
			// determine exit status.
			org.oddjob.state.State state = lastStateEvent.getState();
			if (state.isException()) {
				logger.error("Oddjob terminating JVM with status -1. Oddjob state [" + state + "].", 
					lastStateEvent.getException());
				// really really bad but how else to get the state out to the OS.
				Runtime.getRuntime().halt(-1);
				
			} else if (state.isIncomplete()) {
				logger.info("Oddjob terminating JVM with status 1. Oddjob state [" + state + "].");			
				Runtime.getRuntime().halt(1);
			}
			else {
				logger.info("Oddjob complete. Oddjob state [" + state + "].");			
			}
		}
	}
}
