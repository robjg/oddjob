package org.oddjob.framework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.logging.LogEnabled;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;

/**
 * A utility class to provide wait until stopped functionality.
 * <p>
 * The default timeout is 5 seconds before a {@link FailedToStopException}
 * is thrown.
 * 
 * @author rob
 *
 */
public class StopWait {

	private final Stateful stateful;
	
	private final Logger logger;
	
	private final long timeout;
	
	public StopWait(Stateful stateful) {
		this(stateful, 5000);
	}
	
	public StopWait(Stateful stateful, long timeout) {
		this.stateful = stateful;
		if (stateful instanceof LogEnabled) {
			logger = Logger.getLogger(((LogEnabled) stateful).loggerName());
		}
		else {
			logger = Logger.getLogger(stateful.getClass());
		}
		this.timeout = timeout;
	}
	
	public void run() throws FailedToStopException {		

		if (new IsStoppable().test(
				stateful.lastStateEvent().getState())) {
			doWait();
		}
	}
	
	private void doWait() throws FailedToStopException {		
		
		final BlockingQueue<State> handoff = new LinkedBlockingQueue<State>();
		
		class StopListener implements StateListener {
			
			@Override
			public void jobStateChange(StateEvent event) {
				handoff.add(event.getState());
			}
		};
		
		StopListener listener = new StopListener();
				
		stateful.addStateListener(listener);
		
		try {
			while (true) {

				State state = handoff.poll(timeout, TimeUnit.MILLISECONDS);
				if (state == null) {
					throw new FailedToStopException(stateful);
				}
				if (!state.isStoppable()) {
					return;
				}
				logger.debug("[" + stateful + "] is " + 
						state + ", waiting to stop...");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {
			stateful.removeStateListener(listener);
		}
	}	
}
