package org.oddjob.jobs.job;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SerializableJob;
import org.oddjob.state.MirrorState;
import org.oddjob.util.OddjobConfigException;

/**
 * @oddjob.description This job will run another job. It is intended
 * for starting services or jobs on remote servers which is why it is
 * named start. If it used on a local job it will block until the local
 * job has run.
 * 
 * <p>
 * Unlike the {@link RunJob}, this job will not monitor or reflect the
 * state of the started job. To monitor the state of the started job the
 * job could be followed by a {@link MirrorState}.
 * <p>
 * The start job won't reset the job to be started. If the job to start
 * isn't started because it's in the wrong state this job will still
 * COMPLETE. This job can be preceded by a {@link ResetJob} if resetting
 * is required. 
 * 
 * @oddjob.example
 * 
 * Starting a service. A folder contains a choice of services. The service
 * id to use is provided at runtime with a property such as 
 * -DpriceService=nonCachingPriceService. The selected service is started
 * and used by the Pricing Job.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/job/StartJobExample.xml} 
 * 
 * @author Rob Gordon
 */

public class StartJob extends SerializableJob {
    private static final long serialVersionUID = 2012043000L;

	/** 
	 * @oddjob.property
	 * @oddjob.description The job to start
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
