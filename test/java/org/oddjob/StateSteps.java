package org.oddjob;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;

public class StateSteps {
	private static final Logger logger = Logger.getLogger(StateSteps.class);
	
	private Stateful stateful;
	
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
			logger.info("Received " + event.getState() + 
					" from [" + event.getSource() + "]");
			
			if (index >= steps.length) {
				failureMessage = 
					"More states than expected: " + event.getState() + 
					" (index " + index + ")";
			}
			else {
				if (event.getState() == steps[index]) {
					if (++index == steps.length) {
						done = true;
						synchronized(this) {
							notifyAll();
						}
					}
				}
				else {
					done = true;
					failureMessage = 
							"Expected " + steps[index] + 
							", was " + event.getState() + 
							" (index " + index + ")";
					synchronized(this) {
						notifyAll();
					}
				}
			}
		}
	};	
	
	public void startCheck(final State... steps) {
		if (listener != null) {
			throw new IllegalStateException("Check in progress!");
		}
		if (steps == null || steps.length == 0) {
			throw new IllegalStateException("No steps!");
		}
		
		this.listener = new Listener(steps);
		
		stateful.addStateListener(listener);
	}

	public void checkNow() {
		
		try {
			if (listener.done) {
				if (listener.failureMessage != null) {
					throw new IllegalStateException(listener.failureMessage);
				}
			}
			else {
				throw new IllegalStateException(
						"Not enough states: expected " + 
						listener.steps.length + " " + 
						Arrays.toString(listener.steps) + 
						", was only first " + listener.index + ".");
			}
		}
		finally {
			stateful.removeStateListener(listener);
			listener = null;
		}
	}
	
	public void checkWait() throws InterruptedException {
		if (listener == null) {
			throw new IllegalStateException("No Check In Progress.");
		}
		
		logger.info("Waiting" +
				" on [" + stateful + "] to have states " + 
				Arrays.toString(listener.steps));
		
		while (!listener.done) {
			synchronized(listener) {
				listener.wait(timeout);
			}
		}
		
		checkNow();
		logger.info("Waiting complete");
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
}
