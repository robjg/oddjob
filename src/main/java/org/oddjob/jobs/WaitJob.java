package org.oddjob.jobs;

import java.util.LinkedList;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.scheduling.ExecutorThrottleType;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.State;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

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
 * {@oddjob.xml.resource org/oddjob/jobs/WaitForExample.xml}
 * 
 * @author Rob Gordon
 *
 */

public class WaitJob extends SimpleJob 
		implements Stoppable {
	
	private static final long DEFAULT_WAIT_SLEEP = 1000;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The wait delay in milliseconds. 
	 * @oddjob.required No if for property is set, otherwise yes.
	 */
	private volatile long pause;

	/** 
	 * @oddjob.property for
	 * @oddjob.description The property to wait for. 
	 * @oddjob.required No.
	 */
	private volatile Object forProperty;
	
	private volatile boolean forSet;
	
	/** 
	 * @oddjob.property state
	 * @oddjob.description A state condition to wait for. When this is 
	 * set this job will wait for the job referenced with the <code>
	 * for</code> property match the given state condition. 
	 * See the Oddjob User guide for a full list of state conditions.
	 * @oddjob.required No.
	 */
	private volatile StateCondition state;
	
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
			stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
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
		
		final long waitBetweenChecks;
		if (pause == 0) {
			waitBetweenChecks = DEFAULT_WAIT_SLEEP;
		}
		else {
			waitBetweenChecks = pause;
		}
		
		final LinkedList<State> states = new LinkedList<State>();
		
		StateListener listener = new StateListener() {
			public void jobStateChange(StateEvent event) {
				synchronized (states) {
					states.add(event.getState());
					stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
						public void run() {
							stateHandler().wake();
						}
					});
				}
			}
		};
		
		((Stateful) forProperty).addStateListener(listener);
		
		while (!stop) {

			State now = null;
			
			synchronized (states) {
				if (!states.isEmpty()) {
					now = states.removeFirst();
					logger().debug("State received "+ now);
				}
			}
			
			if (now != null && state.test(now)) {
				logger().debug("State matches " + state);
				break;
			}
			
			logger().debug("Waiting for state to match " + state);
			
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
