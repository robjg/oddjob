package org.oddjob.state;

import java.util.Date;

import org.oddjob.Stateful;

/**
 * Pass on state. Generally used to reflect the state of children.
 * 
 * @author rob
 *
 */
public class StateExchange {

	private final StateChanger recipient;
	
	private final Stateful source;
	
	private boolean running;

	private final JobStateListener stateListener = 
			new AbstractJobStateListener() {
		@Override
		protected void jobStateReady(Stateful source, Date time) {
			recipient.setJobState(JobState.READY, time);
		}
		
		@Override
		protected void jobStateExecuting(Stateful source, Date time) {
			recipient.setJobState(JobState.EXECUTING, time);
		}
		
		@Override
		protected void jobStateComplete(Stateful source, Date time) {
			recipient.setJobState(JobState.COMPLETE, time);
		}
		
		@Override
		protected void jobStateNotComplete(Stateful source, Date time) {
			recipient.setJobState(JobState.INCOMPLETE, time);
		}
	
		@Override
		protected void jobStateException(Stateful source, Date time,
				Throwable throwable) {
			recipient.setJobStateException(throwable, time);
		}
	};
	
	public StateExchange(Stateful source, StateChanger recipient) {
		this.source = source;
		this.recipient = recipient;
	}
	
	public void start() {
		synchronized (this) {
			if (running) {
				return;
			}
			running = true;
		}
		source.addJobStateListener(stateListener);
	}
	
	public void stop() {
		synchronized (this) {
			running = false;
		}
		source.removeJobStateListener(stateListener);
	}
	
	public boolean isRunning() {
		synchronized(this) {
			return running;
		}
	}
}
