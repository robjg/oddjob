package org.oddjob.jobs.structural;


import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.OptionallyTransient;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.SequentialHelper;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;
import org.oddjob.util.Restore;

/**
 * @oddjob.description Executes it's children in a sequence one after the
 * other. The sequence will only continue to be executed if each child 
 * COMPLETEs. If a child is INCOMPLETE, or throws an EXCEPTION then execution
 * will terminate and this job's state will reflect that of the 
 * failed child.
 * <p>
 * This behaviour can be changed by setting the <b><code>independent</code></b>
 * property which will cause execution to continue regardless of the last
 * executed child state. 
 * 
 * <h4>State Operator</h4>
 * 
 * The <b><code>stateOperator</b></code> property changes the way in which
 * this jobs state reflects its child states. Oddjob currently supports the
 * following State Operators:
 * <dl>
 *  <dt>ACTIVE</dt>
 *  <dd>If any child is EXECUTING, ACTIVE or STARTING this job's state
 *  will be ACTIVE. Otherwise, if a child is STARTED, this job's state 
 *  will be STARTED. Otherwise, if a child is READY, this job's state will
 *  be READY. Otherwise, this job's state will reflect the worst state of
 *  the child jobs.</dd>
 *  <dt>WORST</dt>
 *  <dd>This job's state will be EXCEPTION or INCOMPLETE if any of the
 *  child job's are in this state. Otherwise the rules for ACTIVE apply.</dd>
 *  <dt>SERVICES</dt>
 *  <dd>This state operator is designed for starting services. This job
 *  will COMPLETE when all services are STARTED. If any
 *  services fails to start this job reflects the EXCEPTION state. 
 *  Because this job, when using this state operator, completes even though 
 *  it's children are running, this job is analogous to creating daemon 
 *  threads in that the services will not stop Oddjob from shutting down 
 *  once all other jobs have completed.</dd>
 * </dl>
 *  
 * <h4>Stopping</h4>
 * As with other structural jobs, when this job is stopping, either because
 * of a manual stop, or during Oddjob's shutdown cycle, the child jobs and
 * services will still be stopped in an reverse order.
 * 
 * <h4>Persistence</h4>
 * If this job has an Id and Oddjob is running with a Persister, then
 * this job's state will be persisted when it changes. Thus a COMPLETE
 * state will be persisted once all child jobs have completed. If Oddjob
 * is restarted at this point the COMPLETE state of this job will stop 
 * the child job's from re-running, if though they themselves might not
 * have been persisted. To stop this job from being persisted set the 
 * <code>transient</code> property to true. Not that when starting
 * services with this job, persistence is probably not desirable as
 * it will stop the services from re-starting.
 * 
 * <h4>Re-running Child Jobs</h4>
 * 
 * If the failed job is later run manually and completes this Job will
 * reflect the new state. As such it is useful as a trigger point for 
 * the completion of a sequence of jobs.
 * 
 * 
 * @oddjob.example
 * 
 * A simple sequence of two jobs.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/SimpleSequentialExample.xml}
 * 
 * 
 * @oddjob.example
 * 
 * Starting two services. To perform odd jobs, in a workshop for instance,
 * this first 'job' is to turn on the lights and turn on any machines
 * required. The service manager encompasses this idea - and this example
 * embelishes the idea. Real odd jobs for Oddjob will involve activities 
 * such as starting services such as a data source or a server connection.
 * The concept however is still the same.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ServiceManagerExample.xml}
 * 
 * The services are started in order. Once both services have started
 * a job is performed that requires both services. If this configuration
 * were running from the command line, Oddjob would stop the services
 * as it shut down. First the machine would be turned of and then finally
 * the lights would be turned out. 
 * 
 * @author Rob Gordon 
 */
public class SequentialJob extends StructuralJob<Object>
			implements Structural, Stoppable, OptionallyTransient {
	private static final long serialVersionUID = 20111017;
	
	/** Are children independent? i.e does failure stop the sequence. */
	private volatile boolean independent;
	
	/**
	 * @oddjob.property transient
	 * @oddjob.description Is this job transient. If true state will not
	 * be persisted.
	 * @oddjob.required No, default is false.
	 * 
	 * @param stateOperator The state operator to be applied to children's
	 * states to derive our state.
	 */
	private volatile boolean _transient;
	
	/**
	 * @oddjob.property stateOperator
	 * @oddjob.description Set the way the children's state is 
	 * evaluated and reflected by the parent. Values can be WORST, 
	 * ACTIVE, or SERVICES.
	 * @oddjob.required No, default is WORST.
	 * 
	 * @param stateOperator The state operator to be applied to children's
	 * states to derive our state.
	 */
	@ArooaAttribute
	public void setStateOperator(StateOperator stateOperator) {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {		
			this.structuralState.setStateOperator(stateOperator);
		}
	}
	
	public StateOperator getStateOperator() {
		return this.structuralState.getStateOperator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.framework.StructuralJob#getStateOp()
	 */
	@Override
	protected StateOperator getInitialStateOp() {
		return new WorstStateOp();
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
			if (!(independent || new SequentialHelper().canContinueAfter(child))) {						
				logger().info("Child [" + child + "] failed. Can't continue.");
				break;
			}
		}
	}
	
	public boolean isIndependent() {
		return independent;
	}
	
	/**
	 * Set whether children are considered dependent (false, default)
	 * or independent (true).
	 * 
	 * @oddjob.property independent
	 * @oddjob.description Whether the child jobs are independent or not.
	 * @oddjob.required Default is dependent child jobs.
	 * 
	 * @param independent flag value to set
	 */
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.framework.OptionallyTransient#isTransient()
	 */
	public boolean isTransient() {
		return _transient;
	}
	
	public void setTransient(boolean _transient)	 {
		this._transient = _transient;
	}
}
