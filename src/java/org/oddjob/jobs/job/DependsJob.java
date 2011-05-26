package org.oddjob.jobs.job;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;
import org.oddjob.scheduling.Trigger;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;
import org.oddjob.state.StateMemory;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description A job which depends on another job. 
 * <ul>
 * <li>If the other job is in a READY state, this job will run 
 * the other job.</li>
 * <li>If the other job is in an EXECUTING state, this job 
 * will wait.</li>
 * <li>If the other job has finished this job will reflect the 
 * completion state.</li>
 * </ul>
 * 
 * This job is intended to simulate Ant's dependency like
 * functionality however it's usefulness is currently 
 * questionable. Solutions using {@link Trigger} may well be
 * more elegant.
 * 
 * 
 * @author Rob Gordon
 */

public class DependsJob extends SimpleJob implements Stoppable,
		JobStateListener {
    private static final long serialVersionUID = 20050806;

	/**
	 * @oddjob.property
	 * @oddjob.description Job to depend on.
	 * @oddjob.required Yes.
	 */
	private transient Stateful job;

	private transient volatile JobStateEvent event;

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
			job.addJobStateListener(this);
			while (!stop) {
				JobState state = event.getJobState();
				logger().debug("State is [" +  state + "]");
				long sleep = 10;
				if (state == JobState.READY) {
					if (job instanceof Runnable) {
						StateMemory remember = new StateMemory();
						remember.run((Runnable) job);
						if (remember.getJobState() == JobState.COMPLETE) {
							return 0;
						} else if (remember.getJobState() == JobState.INCOMPLETE) {
							return 1;
						} else if (remember.getJobState() == JobState.EXCEPTION) {
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
				} else if (state == JobState.COMPLETE) {
					return 0;
				} else if (state == JobState.INCOMPLETE) {
					return 1;
				} else if (state == JobState.EXCEPTION) {
					throw event.getException();
				} else {
					logger().debug("[" + job + "] still executing...");
					sleep = 1000;
				}
				sleep(sleep);
			}

			return 0;
		} finally {
			job.removeJobStateListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.oddjob.state.JobStateListener#jobStateChange(org.oddjob.state.JobStateEvent)
	 */
	public void jobStateChange(JobStateEvent event) {
		this.event = event;
		synchronized (this) {
			notifyAll();
		}
	}
}
