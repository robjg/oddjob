package org.oddjob.jobs.structural;


import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;
import org.oddjob.framework.Transient;
import org.oddjob.state.SequentialHelper;
import org.oddjob.state.ServiceManagerStateOp;
import org.oddjob.state.StateOperator;

/**
 * @oddjob.description A Service Manager. The purpose of this job is 
 * to start services, it completes when all services are started. If any
 * services fails to start this job stops the services it's already started
 * and flags an exception.
 * <p>
 * Because this job completes even though it's children are active this
 * job is analogous to created daemon threads in that the services will
 * not stop Oddjob from shutting down once all other jobs have completed.
 * During Oddjob's shutdown cycle the services will still be stopped in an 
 * orderly fashion.
 * <p>
 * The state of this ServiceManager will not be persisted by Oddjob, so
 * on start-up it will always attempt to start it's services.
 * <p>
 * If this job is used with standard jobs this job will behave much like
 * {@link SequentialJob} except that when a child job is re-run the 
 * reflected state will be one of READY, INCOMPLETE, COMPLETE or EXCEPTION
 * depending on the other child jobs.
 * <p>
 * Services will be stopped in revere order so that if one service depends
 * on another, the dependent service will be stopped first.
 * 
 * @oddjob.example
 * 
 * Starting two services.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/SimpleServiceExample.xml}
 * 
 * The services are started in order. Once both services have 
 * @author Rob Gordon
 *  
 */
public class ServiceManager extends StructuralJob<Object>
			implements Structural, Stoppable, Transient {
	private static final long serialVersionUID = 2012051000L;
	
	@Override
	protected StateOperator getStateOp() {
		return new ServiceManagerStateOp();
	}
	
	/**
	 * Add a child.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setJobs(int index, Object child) {
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}		
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	public void execute() throws Exception {		
		
		for (Object child : childHelper) {
			if (stop) {
				stop = false;
				break;
			}
			
			if (!(child instanceof Runnable)) {
				logger().info("Not Executing [" + child + "] as it is not a job.");
			}
			else {
				Runnable job = (Runnable) child;
				logger().info("Executing child [" + job + "]");
				
				job.run();
			}
			
			// Test we can still continue children.
			if (!(new SequentialHelper().canContinueAfter(child))) {						
				logger().info("Child [" + child + "] failed. Can't continue.");
				break;
			}
		}
		
		
	}	
}
