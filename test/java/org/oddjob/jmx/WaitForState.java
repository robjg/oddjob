/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx;

import org.apache.log4j.Logger;
import org.oddjob.Stateful;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class WaitForState implements JobStateListener {
	private static final Logger logger = Logger.getLogger(WaitForState.class);
	
	private final Stateful stateful; 
	private JobState jobState;
	
	public WaitForState(Object o) {
		stateful = (Stateful) o;
	}
	
	public void waitFor(JobState required) {
		stateful.addJobStateListener(this);
		try {
			synchronized (this) {
				while (jobState != required) {
					logger.debug("Waiting for [" + stateful +
							"] to be [" + required + "]");
					wait();
				}
			}
		} catch (InterruptedException e) {
		} finally {
			stateful.removeJobStateListener(this);
		}
		
	}	

	synchronized public void jobStateChange(JobStateEvent event) {
		jobState = event.getJobState();
		notifyAll();
	}
	

}