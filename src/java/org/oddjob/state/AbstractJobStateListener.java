package org.oddjob.state;

import java.util.Date;

import org.oddjob.Stateful;


/**
 * An implementation of a JobStateListener designer to be overriden.
 * 
 * @author Rob Gordon
 */

abstract public class AbstractJobStateListener implements JobStateListener {

	/**
	 * This is the catch all method that all other methods call if they 
	 * haven't been overridden.
	 * 
	 * @param event The state event.
	 */
	public final void jobStateChange(JobStateEvent event) {
		JobState state = event.getJobState();
		Stateful source = (Stateful)event.getSource();
		Date time = event.getTime();
		Throwable throwable = event.getException();
		
		switch (state) {
		case READY:
			jobStateReady(source, time);
			break;
		case EXECUTING:
			jobStateExecuting(source, time);
			break;
		case COMPLETE:
			jobStateComplete(source, time);
			break;
		case INCOMPLETE:
			jobStateNotComplete(source, time);
			break;
		case EXCEPTION:
			jobStateException(source, time, throwable);
			break;
		case DESTROYED:
			jobStateDestroyed(source, time);
			break;
		default:
			throw new IllegalStateException("Unknown state! - this should never happen.");
		}
	}

	protected void jobStateReady(Stateful source, Date time) {
	}
	
	protected void jobStateExecuting(Stateful source, Date time) {
	}
	
	protected void jobStateComplete(Stateful source, Date time) {
	}
	
	protected void jobStateNotComplete(Stateful source, Date time) {
	}
	
	protected void jobStateException(Stateful source, Date time, Throwable throwable) {
	}	
	
	protected void jobStateDestroyed(Stateful source, Date time) {
	}
}
