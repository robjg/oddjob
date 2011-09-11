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
 * @author Rob Gordon
 */

public class ParallelJob extends SimultaneousStructural {
	private static final long serialVersionUID = 2009031800L;
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
}
