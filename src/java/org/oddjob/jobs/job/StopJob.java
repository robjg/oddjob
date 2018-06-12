package org.oddjob.jobs.job;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.extend.SerializableJob;
import org.oddjob.jmx.JMXClientJob;

/**
 * @oddjob.description A job which stops another job. 
 * <p>
 * Normally The stop job will not complete until the job it is
 * stopping is in a not executing state. Therefore if the
 * stop job is attempting to stop a parent job of itself the stop
 * job could block indefinitely. This case is detected and the job
 * enters an Exception state.
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>{@link JMXClientJob} has an example where the stop
 *  job is used to stop a client once the connection is no 
 *  longer needed.</li>
 * </ul>
 * 
 * @author Rob Gordon
 */
public class StopJob extends SerializableJob 
implements Stoppable {
    private static final long serialVersionUID = 20050806;

	/** 
	 * @oddjob.property
	 * @oddjob.description Job to stop 
	 * @oddjob.required Yes.
	 */
	private transient Stoppable job;
	
	/** Used to check we're not stopping ourself. */
	private transient volatile Thread thread; 
		
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The node to stop.
	 */
	@ArooaAttribute
	public void setJob(Stoppable node) {
		this.job = node;
	}

	/**
	 * Get the node to stop.
	 * 
	 * @return The node.
	 */
	public Stoppable getJob() {
		return this.job;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		
		if (job == null) {
			throw new NullPointerException("No Job to Stop");
		}

		logger().info("Stopping [" + job + "]");
		
		thread = Thread.currentThread();
		try {
			job.stop();
		}
		finally {
			thread = null;
		}
		
		return 0;	
	}
	
	@Override
	protected void onStop() throws FailedToStopException {
		
		if (thread == Thread.currentThread()) {
			throw new FailedToStopException(this, 
					"Can't stop ourself. " +
					"Maybe use a trigger to stop asynchronously.");
		}
	}
}
