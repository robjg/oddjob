package org.oddjob;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;

/**
 * Test Utility class to track state changes.
 * 
 * @author rob
 *
 */
public class StateSteps {
	private static final Logger logger = Logger.getLogger(StateSteps.class);
	
	private final Stateful stateful;
	
	private Listener listener;
	
	private long timeout = 5000L;
	
	public StateSteps(Stateful stateful) {
		if (stateful == null) {
			throw new NullPointerException("No Stateful.");
		}
		this.stateful = stateful;
	}
	
	class Listener implements StateListener {
		
		private final State[] steps;
		
		private int index;
		
		private boolean done;
		
		private String failureMessage;
		
		public Listener(State[] steps) {
			this.steps = steps;
		}
		
		@Override
		public void jobStateChange(StateEvent event) {
			synchronized (StateSteps.this) {				
				String position;
				if (failureMessage != null) {
					position = "(failure pending)";
				}
				else {
					position = "for index [" + index + "]";
				}

				logger.info("Received [" + event.getState() + 
						"] " + position + " from [" + event.getSource() + "]");

				if (failureMessage != null) {
					return;
				}
				
				if (index >= steps.length) {
					failureMessage = 
							"More states than expected: " + event.getState() + 
							" (index " + index + ")";
				}
				else {
					if (event.getState() == steps[index]) {
						if (++index == steps.length) {
							done = true;
							StateSteps.this.notifyAll();
						}
					}
					else {
						done = true;
						failureMessage = 
								"Expected " + steps[index] + 
								", was " + event.getState() + 
								" (index " + index + ")";
						StateSteps.this.notifyAll();
					}
				}
			}
		}
		
		public synchronized boolean isDone() {
			return done;
		}
	};	
	
	public synchronized void startCheck(final State... steps) {
		if (listener != null) {
			throw new IllegalStateException("Check in progress!");
		}
		if (steps == null || steps.length == 0) {
			throw new IllegalStateException("No steps!");
		}
		
		logger.info("Starting check on [" + stateful + "] to have states " + 
				Arrays.toString(steps));
		
		this.listener = new Listener(steps);
		
		stateful.addStateListener(listener);
	}

	public synchronized void checkNow() {
		
		try {
			if (listener.isDone()) {
				if (listener.failureMessage != null) {
					throw new IllegalStateException(listener.failureMessage);
				}
			}
			else {
				throw new IllegalStateException(
						"Not enough states for [" + stateful + "]: expected " + 
						listener.steps.length + " " + 
						Arrays.toString(listener.steps) + 
						", was only first " + listener.index + ".");
			}
		}
		catch (IllegalStateException e) {
			logger.error(e);
			throw e;
		}
		finally {
			stateful.removeStateListener(listener);
			listener = null;
		}
	}
	
	public synchronized void checkWait() throws InterruptedException {
		if (listener == null) {
			throw new IllegalStateException("No Check In Progress.");
		}
		
		logger.info("Waiting" +
				" on [" + stateful + "] to have states " + 
				Arrays.toString(listener.steps));

		if (!listener.isDone()) {

			wait(timeout);

			logger.info("Woken or Timedout " +
					" on [" + stateful + "] to have states " + 
					Arrays.toString(listener.steps));
		}

		checkNow();
		
		logger.info("Waiting complete on [" + stateful + "]");
	}

	public synchronized long getTimeout() {
		return timeout;
	}

	public synchronized void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
}
