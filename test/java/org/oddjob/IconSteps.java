package org.oddjob;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;

public class IconSteps {
	private static final Logger logger = Logger.getLogger(IconSteps.class);
	
	private Iconic iconic;
	
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
		
		private IllegalStateException failureException;
		
		public Listener(String[] steps) {
			this.steps = steps;
		}
		
		@Override
		public void iconEvent(IconEvent event) {
			String position;
			if (failureException != null) {
				position = "(failure pending)";
			}
			else {
				position = "for index [" + index + "]";
			}
			
			logger.info("Received Icon Id [" + event.getIconId() + 
					"] " + position + " from [" + event.getSource() + "]");
			
			if (index >= steps.length) {
				failureException = new IllegalStateException(
					"More icons than expected: " + event.getIconId() + 
					" (index " + index + ")");
			}
			else {
				if (event.getIconId() == steps[index]) {
					if (++index == steps.length) {
						done = true;
						synchronized(this) {
							notifyAll();
						}
					}
				}
				else {
					done = true;
					failureException = new IllegalStateException(
							"Expected " + steps[index] + 
							", was " + event.getIconId() + 
							" (index " + index + ")");
					synchronized(this) {
						notifyAll();
					}
				}
			}
		}
	};	
	
	public void startCheck(String... steps) {
		if (listener != null) {
			throw new IllegalStateException("Check in progress!");
		}
		if (steps == null || steps.length == 0) {
			throw new IllegalStateException("No steps!");
		}
		
		this.listener = new Listener(steps);
		
		iconic.addIconListener(listener);
	}

	public void checkNow() {
		
		try {
			if (listener.done) {
				if (listener.failureException != null) {
					throw listener.failureException;
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
			iconic.removeIconListener(listener);
			listener = null;
		}
	}
	
	public void checkWait() throws InterruptedException {
		if (listener == null) {
			throw new IllegalStateException("No Check In Progress.");
		}
		
		logger.info("Waiting" +
				" on [" + iconic + "] to have states " + 
				Arrays.toString(listener.steps));
		
		if (!listener.done) {
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
