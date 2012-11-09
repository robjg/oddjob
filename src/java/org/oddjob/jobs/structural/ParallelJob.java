package org.oddjob.jobs.structural;

import org.oddjob.framework.SimultaneousStructural;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;

/**
 * @oddjob.description
 * 
 * A job which executes it's child jobs in parallel.
 * <p>
 * The return state for this job depends on the return
 * states of all the children - Complete if all the 
 * children complete, exception if there are any exceptions
 * in the children, 
 * or not complete if any of the children fail to complete. 
 * 
 * @oddjob.example
 * 
 * Two jobs running in parallel.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/SimpleParallelExample.xml}
 * 
 * @oddjob.example
 * 
 * Two services started in parallel. This might be quite useful if the
 * services took a long time to start - maybe because they loaded a lot
 * of data into a cache for instance.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ParallelServicesExample.xml}
 * 
 * @author Rob Gordon
 */
public class ParallelJob extends SimultaneousStructural {
	private static final long serialVersionUID = 2009031800L;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description Should the execution thread of this job wait 
	 * for the execution threads of the child jobs.
	 * <p>
	 * This property 
	 * re-introduces the default behaviour of parallel before version 1.0. 
	 * Behaviour was changed to encourage the use of event driven
	 * configuration that didn't cause a thread to wait by using 
	 * {@link org.oddjob.state.CascadeJob} or 
	 * {@link org.oddjob.scheduling.Trigger}.
	 * <p>
	 * There are situations where this is really convenient as otherwise
	 * large reworking of the configuration is required. If possible - 
	 * it is better practice to try and use the job state.
	 * 
	 * @oddjob.required No. Defaults to false
	 */
	private volatile boolean join;
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}

	@Override
	public boolean isJoin() {
		return join;
	}

	public void setJoin(boolean join) {
		this.join = join;
	}
}
