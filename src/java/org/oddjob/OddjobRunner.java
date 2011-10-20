package org.oddjob;

import org.apache.log4j.Logger;
import org.oddjob.state.IsStoppable;
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
	
	private final Oddjob oddjob;
	
	private volatile boolean destroying = false;

	private final long killerTimeout;
	
	public OddjobRunner(Oddjob oddjob) {
		this.oddjob = oddjob;
		
		String timeoutProperty = System.getProperty(KILLER_TIMEOUT_PROPERTY);
		long timeout = 0L;
		if (timeoutProperty != null) {
			try {
				timeout = new Long(timeoutProperty).longValue();
			} catch (NumberFormatException e) {
				logger.debug("Unparseable timeout property " + 
						timeoutProperty);
			}
		}
		if (timeout == 0L) {
			killerTimeout = DEFAULT_KILLER_TIMEOUT;
		}
		else {
			killerTimeout = timeout;
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
			if (!destroying) {
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
	
	class ShutdownHook extends Thread {
		
		Thread killer;
		
		public void run() {
			
			logger.info("Shutdown Hook Executing.");
			
			// killer will just kill process if we can't stop in 15 sec
			killer = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(killerTimeout);
					}
					catch (InterruptedException e) {
						return;
					}
					logger.error("Failed to stop Oddjob nicely, using halt(-1)");
					Runtime.getRuntime().halt(-1);
				}
			});
			
			// start the killer
			killer.setDaemon(true);
			killer.start();
			
			// try stopping oddjob.
			try {
				oddjob.stop();
			} catch (FailedToStopException e) {
				logger.error(e.getMessage());
			}

			// remember the last job event because we can't get it once
			// Oddjob is destroyed.
			StateEvent lastJobStateEvent = oddjob.lastStateEvent();

			destroying = true;
			oddjob.onDestroy();
				
			// determine exit status.
			org.oddjob.state.State state = lastJobStateEvent.getState();
			if (state.isException()) {
				logger.error("Oddjob complete. State [" + state + "].", 
					lastJobStateEvent.getException());
			} else {
				logger.info("Oddjob complete. State [" + state + "].");			
			}
			
			if (!state.isComplete()) {
				// really really bad but how else to get the state out.
				if (state.isIncomplete()) {
					Runtime.getRuntime().halt(1);
				}
				else {
					Runtime.getRuntime().halt(-1);
				}
			}
		}
	}

	
	

	
}
