package org.oddjob.jobs.job;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.JobState;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateMemory;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description This job is deprecated, use {@link RunJob} instead. 
 * <p>
 * A job which depends on another job. 
 * <ul>
 * <li>If the other job is in a READY state, this job will run 
 * the other job.</li>
 * <li>If the other job is in an EXECUTING state, this job 
 * will wait.</li>
 * <li>If the other job has finished this job will reflect the 
 * completion state.</li>
 * </ul>
 * 
 * This job was intended to simulate Ant's dependency like
 * functionality but the run job is better.
 * 
 * @deprecated Use {@link RunJob} instead.
 * @author Rob Gordon
 */

public class DependsJob extends SimpleJob implements Stoppable,
		StateListener {

	/**
	 * @oddjob.property
	 * @oddjob.description Job to depend on.
	 * @oddjob.required Yes.
	 */
	private transient Stateful job;

	private transient volatile StateEvent event;

	public DependsJob() {
		logger().warn("Depends Job is deprecated. Use run instead.");
	}
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node
	 *            The node to stop.
	 */
	@ArooaAttribute
	synchronized public void setJob(Stateful node) {
		this.job = node;
	}

	/**
	 * Get the node to stop.
	 * 
	 * @return The node.
	 */
	synchronized public Stateful getJob() {
		return this.job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Throwable {
		if (job == null) {
			throw new OddjobConfigException("Job must be set.");
		}
		try {
			job.addStateListener(this);
			while (!stop) {
				State state = event.getState();
				logger().debug("State is [" +  state + "]");
				long sleep = 10;
				if (state == JobState.READY) {
					if (job instanceof Runnable) {
						StateMemory remember = new StateMemory();
						remember.run((Runnable) job);
						if (remember.getJobState().isComplete()) {
							return 0;
						} else if (remember.getJobState().isIncomplete()) {
							return 1;
						} else if (remember.getJobState().isException()) {
							throw remember.getThrowable();
						}
						// this shouldn't happen but something else
						// could change the state before we ran it.
						// go round again...
					} 
					else {
						// if job's not runnable we could be waiting a long time.
						sleep = 0;
					}
				} else if (state.isComplete()) {
					return 0;
				} else if (state.isIncomplete()) {
					return 1;
				} else if (state.isException()) {
					throw event.getException();
				} else {
					logger().debug("[" + job + "] still executing...");
					sleep = 1000;
				}
				sleep(sleep);
			}

			return 0;
		} finally {
			job.removeStateListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.oddjob.state.JobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
	 */
	@Override
	public void jobStateChange(StateEvent event) {
		this.event = event;
		synchronized (this) {
			notifyAll();
		}
	}
}
