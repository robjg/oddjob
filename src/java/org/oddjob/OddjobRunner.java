package org.oddjob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.framework.StopWait;
import org.oddjob.state.StateEvent;

/**
 * A Wrapper for running Oddjob that ensures a smooth shutdown.
 * <p>
 * If Oddjob doesn't terminate then a timeout will just kill the JVM. The
 * timeout is configurable but defaults to 15 seconds.
 * 
 * @author rob
 *
 */
public class OddjobRunner implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(OddjobRunner.class);
		
	public static final String KILLER_TIMEOUT_PROPERTY = 
		"oddjob.shutdown.killer.timeout";
	
	public static final long DEFAULT_KILLER_TIMEOUT = 15_000L;
	
	/** The Oddjob we're running. */
	private final Oddjob oddjob;
	
	/** Flag if Oddjob is being destroyed from the Shutdown Hook. */
	private volatile boolean destroying = false;

	/** The killer thread time out. */
	private final long killerTimeout;
	
	private final ExitHandler exitHandler;
	
	@FunctionalInterface
	public interface ExitHandler {
		public void exit(int exitStatus);
	}
	
	public OddjobRunner(Oddjob oddjob) {
		this(oddjob, new ExitHandler() {
			@Override
			public void exit(int exitStatus) {
				// uses halt, not exit as we don't want to invoke the 
				// shutdown hook
				Runtime.getRuntime().halt(exitStatus);
			}
		});
	}
	
	/**
	 * Constructor.
	 * 
	 * @param oddjob The Oddjob to run.
	 */
	public OddjobRunner(Oddjob oddjob, ExitHandler exitHandler) {
		if (oddjob == null) {
			throw new NullPointerException("No Oddjob");
		}
		if (exitHandler == null) {
			throw new NullPointerException("No Exit Handler");
		}
		
		this.oddjob = oddjob;
		this.exitHandler = exitHandler;
		
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
	
	/**
	 * Initialise a shutdown hook. A separate method so that run can
	 * be tested without a shutdown hook.
	 */
	void initShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());		
	}
	
	@Override
	public void run() {
		
		logger.info("Starting Oddjob version " + oddjob.getVersion());
		
		try {
			oddjob.run();
			
			// This needs to be thought out a bit more.
			// The logic goes along the lines of if the main
			// thread get's here we need to wait until the Oddjob
			// has completed and then we can destroy it. 

			logger.debug("Oddjob execution thread completed." +
						" May wait for Oddjob, it's state is " + 
						oddjob.lastStateEvent().getState() + ".");
				
			// Possibly wait for Oddjob to be in a stopped state.
			new StopWait(oddjob, Long.MAX_VALUE).run();
				
			StateEvent lastStateEvent = oddjob.lastStateEvent();

			logger.debug("Oddjob is finished, state [" + lastStateEvent + "]");
				
			// Destroying Oddjob should allow JVM to exit. 
			logger.debug("Destroying Oddjob.");			
			destroying = true;
			oddjob.destroy();
			
			// determine exit status.
			org.oddjob.state.State state = lastStateEvent.getState();
			if (state.isException()) {
				logger.error("Oddjob terminating JVM with status -1. Oddjob state [" + state + "].", 
					lastStateEvent.getException());
				// really really bad but how else to get the state out to the OS.
				exitHandler.exit(-1);
				
			} else if (state.isIncomplete()) {
				logger.info("Oddjob terminating JVM with status 1. Oddjob state [" + state + "].");			
				exitHandler.exit(1);
			}
			else {
				logger.info("Oddjob complete. Oddjob state [" + state + "].");			
			}
		} 
		catch (Throwable t) {
			logger.error("Exception running Oddjob.", t);
			exitHandler.exit(1);
		}
	}
	
	class Killer implements Runnable {
		
		@Override
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
			exitHandler.exit(-1);
		}
	}
	
	/**
	 * Oddjob's shutdown hook.
	 */
	class ShutdownHook extends OddjobShutdownThread {
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {

			logger.info("Shutdown Hook Executing.");
			
			// killer will just kill process if we can't stop in 15 sec
			Thread killer = new Thread(new Killer(), "Killer-Thread");
			
			// start the killer. This is a daemon so it will be terminated when other thread die.
			logger.debug("Starting killer thread.");
			killer.setDaemon(true);
			killer.start();

			if (!destroying) {
				logger.debug("Stopping [" + oddjob + "]");
				try {
					oddjob.stop();
				} 
				catch (FailedToStopException e) {
					logger.error("Oddjob failed to stop. Terminating VM.", e);
					exitHandler.exit(-1);
				}
			}

			logger.debug("Shutdown hook complete.");
		}
	}
}
