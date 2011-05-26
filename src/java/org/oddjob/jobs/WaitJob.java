package org.oddjob.jobs;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;
import org.oddjob.scheduling.ExecutorThrottleType;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

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
 * <pre><code>
 * &lt;sequential name="Waiting For a Property"&gt;
 *  &lt;jobs&gt;
 *   &lt;variables id="waitvars"/&gt;
 *   &lt;wait for="${waitvars.text}" pause="5000" name="Wait for Variable"/&gt;
 *   &lt;echo text="${waitvars.text}" name="Echo Text"/&gt;
 *  &lt;jobs&gt;
 * &lt;/sequential&gt;
 * </code></pre>
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
	private JobState state;
	
	private boolean not;
		
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
		class Listener implements JobStateListener {
			synchronized public void jobStateChange(JobStateEvent event) {
				synchronized (WaitJob.this) {
					WaitJob.this.notifyAll();
				}
			}
		}
		Listener listener = new Listener();
		
		Stateful stateful = (Stateful) forProperty;
		
		stateful.addJobStateListener(listener);
		
		long waitBetweenChecks = pause;
		if (waitBetweenChecks == 0) {
			waitBetweenChecks = DEFAULT_WAIT_SLEEP;
		}
		
		while (!stop) {

			JobState now = stateful.lastJobStateEvent().getJobState();
			logger().debug("State of [" + forProperty + "] is "+ state);
						
			if (now == state && !not) {
				break;
			}
			if (now != state && not) {
				break;
			}
			logger().debug("Waiting for state " + 
					(not ? "!" : "") + state + ", currently "
					+ now);
			sleep(waitBetweenChecks);
		}
		((Stateful) forProperty).removeJobStateListener(listener);
	}
	
	public Object getFor() {
		return forProperty;
	}

	@ArooaAttribute
	public void setFor(Object forProperty) {
		this.forProperty = forProperty;
		this.forSet = true;
	}
	
	public String getState() {
		if (state == null) {
			return null;
		}
		return state.toString();
	}
	
	public void setState(String state) {
		if (state.startsWith("!")) {
			not = true;
			state = state.substring(state.indexOf("!") + 1);
		}
		this.state = JobState.stateFor(state);
		if (this.state == null) {
			throw new IllegalArgumentException("Unknown state requested [" + state + "]");
		}
	}
}
