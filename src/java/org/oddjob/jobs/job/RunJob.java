package org.oddjob.jobs.job;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description A job which runs another job. The other job can be
 * local or or on a server.
 * <p>
 * This jobs completion state will reflect the success of the run 
 * operation, not the state of job being run.
 * <p>
 * 
 * @oddjob.example
 * 
 * If 'sales' is the id of a {@link org.oddjob.jmx.JMXClientJob}, then 
 * this would run the job fred on the corresponding server.
 * 
 * <pre>
 * &lt;run job="${sales.lookup(fred)}"/&gt;
 * </pre>
 * 
 * @author Rob Gordon
 */

public class RunJob extends SimpleJob {
    private static final long serialVersionUID = 20050806;

	/** 
	 * @oddjob.property
	 * @oddjob.description Job to run
	 * @oddjob.required Yes.
	 */
	private transient Runnable job;

	/**
	 * Set the stop node directly.
	 * 
	 * @param node The job.
	 */
	@ArooaAttribute
	synchronized public void setJob(Runnable node) {
		this.job = node;
	}

	/**
	 * Get the job.
	 * 
	 * @return The node.
	 */
	synchronized public Runnable getJob() {
		return this.job;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected int execute() throws Exception {
		if (job == null) {
			throw new OddjobConfigException("A job to start must be provided.");
		}		
		job.run();
		return 0;	
	}
	
}
