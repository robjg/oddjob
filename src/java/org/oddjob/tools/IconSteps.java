package org.oddjob.tools;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.Iconic;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;

/**
 * Test Utility class to track icon changes.
 * 
 * @author rob
 *
 */
public class IconSteps {
	private static final Logger logger = LoggerFactory.getLogger(IconSteps.class);
	
	private final Iconic iconic;
	
	private Listener listener;
	
	private long timeout = 5000L;
	
	public IconSteps(Iconic iconic) {
		if (iconic == null) {
			throw new NullPointerException("No Iconic.");
		}
		this.iconic = iconic;
	}
	
	class Listener implements IconListener {
		
		private final String[] steps;
		
		private int index;
		
		private boolean done;
		
		private String failureMessage;
		
		public Listener(String[] steps) {
			this.steps = steps;
		}
		
		@Override
		public void iconEvent(IconEvent event) {
			synchronized (IconSteps.this) {
				String position;
				if (failureMessage != null) {
					position = "(failure pending)";
				}
				else {
					position = "for index [" + index + "]";
				}
				
				logger.info("Received Icon Id [" + event.getIconId() + 
						"] " + position + " from [" + event.getSource() + "]");
				
				if (failureMessage != null) {
					return;
				}
				
				if (index >= steps.length) {
					failureMessage = 
						"More icons than expected: " + event.getIconId() + 
						" (index " + index + ")";
				}
				else {
					if (event.getIconId() == steps[index]) {
						if (++index == steps.length) {
							done = true;
							IconSteps.this.notifyAll();
						}
					}
					else {
						done = true;
						failureMessage = 
								"Expected " + steps[index] + 
								", was " + event.getIconId() + 
								" (index " + index + ")";
						
						IconSteps.this.notifyAll();
					}
				}
			}
		}
	};	
	
	public synchronized void startCheck(String... steps) {
		if (listener != null) {
			throw new IllegalStateException("Check in progress!");
		}
		if (steps == null || steps.length == 0) {
			throw new IllegalStateException("No steps!");
		}
		
		logger.info("Starting check on [" + iconic + "] to have icons " + 
				Arrays.toString(steps));
		
		this.listener = new Listener(steps);
		
		iconic.addIconListener(listener);
	}

	public synchronized void checkNow() {
		
		try {
			if (listener.done) {
				if (listener.failureMessage != null) {
					throw new IllegalStateException(listener.failureMessage);
				}
			}
			else {
				throw new IllegalStateException(
						"Not enough icons: expected " + 
						listener.steps.length + " " + 
						Arrays.toString(listener.steps) + 
						", was only first " + listener.index + ".");
			}
		}
		finally {
			iconic.removeIconListener(listener);
			listener = null;
		}
	}
	
	public synchronized void checkWait() throws InterruptedException {
		if (listener == null) {
			throw new IllegalStateException("No Check In Progress.");
		}
		
		logger.info("Waiting" +
				" on [" + iconic + "] to have icons " + 
				Arrays.toString(listener.steps));
		
		if (!listener.done) {
			wait(timeout);
		}
		
		checkNow();
		logger.info("Waiting complete");
	}

	public synchronized long getTimeout() {
		return timeout;
	}

	public synchronized void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
}
