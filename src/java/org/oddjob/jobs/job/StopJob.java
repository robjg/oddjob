package org.oddjob.jobs.job;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.framework.SerializableJob;
import org.oddjob.jmx.JMXClientJob;

/**
 * @oddjob.description A job which stops another job. 
 * <p>
 * Normally The stop job will not complete until the job it is
 * stopping is in a not executing state. Therefore if the
 * stop job is attempting to stop a parent job the stop
 * job will block indefinitely. To resolve this problem the
 * <code>async</code> property was introduced.
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
public class StopJob extends SerializableJob {
    private static final long serialVersionUID = 20050806;

	/** 
	 * @oddjob.property
	 * @oddjob.description Job to stop 
	 * @oddjob.required Yes.
	 */
	private transient Stoppable job;

	/** 
	 * @oddjob.property
	 * @oddjob.description Stop the job asynchronously. Needed if the
	 * job to stop is a parent.
	 * @oddjob.required No.
	 */
	private boolean async;

	/** The executor to use. */
	private volatile transient ExecutorService executor;

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.OddjobAware#setOddjobServices(org.oddjob.OddjobServices)
	 */
	@Inject
	@ArooaHidden
	public void setExecutorService(ExecutorService executor) {
		this.executor = executor;
	}
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The node to stop.
	 */
	@ArooaAttribute
	synchronized public void setJob(Stoppable node) {
		this.job = node;
	}

	/**
	 * Get the node to stop.
	 * 
	 * @return The node.
	 */
	synchronized public Stoppable getJob() {
		return this.job;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		
		if (job == null) {
			throw new NullPointerException("No Job to Stop");
		}

		logger().info("Stopping [" + job + "]" + 
				(async ? " asynchronously" : "") + ".");
		
		if (async) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					ComponentBoundry.push(loggerName(), this);
					try {
						job.stop();
						logger().info("Asyncronous stop complete.");
					} catch (FailedToStopException e) {
						logger().warn(e);
					} finally {
						ComponentBoundry.pop();
					}
				}
			});
		}
		else {
			job.stop();
		}
		
		return 0;	
	}
}
