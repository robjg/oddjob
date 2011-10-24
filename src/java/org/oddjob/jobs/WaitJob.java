package org.oddjob.jobs;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;
import org.oddjob.scheduling.ExecutorThrottleType;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.State;
import org.oddjob.state.StateCondition;

/**
 * @oddjob.description This Job will either wait a given number of milliseconds
 * or will wait for a property or job to become available. 
 * <p>
 * If the for property is provided, then the delay is used as the number of
 * milliseconds between checking if the property is available.
 * 
 * @oddjob.example
 * 
 * The {@link ExecutorThrottleType} has a simple example.
 * 
 * @oddjob.example
 * 
 * This example waits for a variable 'text' to be set. The value could be set
 * across the network or by a another job running in parallel.  
 * 
 * {@oddjob.xml.resource org/oddjob/io/WaitForExample.xml}
 * 
 * @author Rob Gordon
 *
 */

public class WaitJob extends SimpleJob 
		implements Stoppable {
	private static final long serialVersionUID = 20051130; 
	
	private static final long DEFAULT_WAIT_SLEEP = 1000;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The wait delay in milliseconds. 
	 * @oddjob.required No if for property is set, otherwise yes.
	 */
	private long pause;

	/** 
	 * @oddjob.property for
	 * @oddjob.description The property to wait for. 
	 * @oddjob.required No.
	 */
	private Object forProperty;
	
	private boolean forSet;
	
	/** 
	 * @oddjob.property state
	 * @oddjob.description A state to wait for. 
	 * @oddjob.required No.
	 */
	private StateCondition state;
	
	/**
	 * Set the delay time in milli seconds.
	 * 
	 * @param delay The delay time.
	 */
	public void setPause(long delay) {	
		this.pause = delay;
	}
	
	/**
	 * Get the delay time in milli seconds.
	 * 
	 * @return The delay time.
	 */
	public long getPause() {
		return pause;
	}
	
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	public int execute() throws Exception {
		
		if (state != null) {
			if (forProperty == null) {
				throw new IllegalStateException("'for' property must set.");
			}
			if (!(forProperty instanceof Stateful)) {
				throw new IllegalStateException("'for' property must Stateful.");
			}
			waitForState();
		}
		else if (forSet) {
			logger().debug("Waiting for property.");
			waitFor();
		}
		else {
			simpleWait();
		}
		return 0;
	}

	protected void simpleWait() {
		sleep(pause);					
	}

	protected void waitFor() {
		long sleep = pause;
		if (sleep == 0) {
			sleep = DEFAULT_WAIT_SLEEP;
		}
		while (!stop) {
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					configure();
				}
			});
			if (forProperty != null) {
				break;
			}
			sleep(sleep);
		}
	}

	protected void waitForState() {
		class Listener implements StateListener {
			synchronized public void jobStateChange(StateEvent event) {
				synchronized (WaitJob.this) {
					WaitJob.this.notifyAll();
				}
			}
		}
		Listener listener = new Listener();
		
		Stateful stateful = (Stateful) forProperty;
		
		stateful.addStateListener(listener);
		
		long waitBetweenChecks = pause;
		if (waitBetweenChecks == 0) {
			waitBetweenChecks = DEFAULT_WAIT_SLEEP;
		}
		
		while (!stop) {

			State now = stateful.lastStateEvent().getState();
			logger().debug("State of [" + forProperty + "] is "+ state);
				
			if (state.test(now)) {
				break;
			}
			logger().debug("Waiting for state " + 
					 state + ", currently "
					+ now);
			
			sleep(waitBetweenChecks);
		}
		((Stateful) forProperty).removeStateListener(listener);
	}
	
	public Object getFor() {
		return forProperty;
	}

	@ArooaAttribute
	public void setFor(Object forProperty) {
		this.forProperty = forProperty;
		this.forSet = true;
	}
	
	public StateCondition getState() {
		return state;
	}
	
	@ArooaAttribute
	public void setState(StateCondition state) {
		this.state = state;
	}
}
