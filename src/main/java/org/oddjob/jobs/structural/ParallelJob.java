package org.oddjob.jobs.structural;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.OptionallyTransient;
import org.oddjob.framework.extend.SimultaneousStructural;
import org.oddjob.scheduling.ExecutorThrottleType;
import org.oddjob.state.CascadeJob;
import org.oddjob.state.StateOperator;
import org.oddjob.state.AnyActiveStateOp;

/**
 * @oddjob.description
 * 
 * A job which executes it's child jobs in parallel.
 * <p>
 * Once the child jobs are submitted, Oddjob's thread of execution continues
 * on out of this job. The state is set to ACTIVE and will continue to
 * change depending on the state of the child Jobs. The <code>join</code>
 * property can be used to hold the thread of execution until the 
 * submitted jobs have finished executing - but it's use is discouraged. 
 * See the property documentation below for more information.
 * 
 * The state of job, including its modification by the 
 * <code>stateOperator</code> property is identical to {@link SequentialJob}
 * and is well documented there. Likewise with the transient property.
 * 
 * @oddjob.example
 * 
 * Two jobs running in parallel. Note that the order of execution of the
 * two child jobs is not predictable.
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
 * The {@link CascadeJob} will execute the final job only once both services
 * have started, and it will continue be in a STARTED after execution has
 * completed.
 * <p>
 * Adding a SERVICES stateOperator property will mean that parallel is
 * COMPLETE once the services have started and so the whole cascade shows
 * as complete.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/ParallelServicesExample2.xml}
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>{@link ExecutorThrottleType} has an example of limiting the number
 *  of concurrently executing jobs.</li>
 * </ul>
 * 
 * 
 * @author Rob Gordon
 */
public class ParallelJob extends SimultaneousStructural
implements OptionallyTransient {
	
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
	
	/**
	 * @oddjob.property transient
	 * @oddjob.description Is this job transient. If true state will not
	 * be persisted.
	 * @oddjob.required No, default is false.
	 * 
	 */
	private volatile boolean _transient;
	
	/**
	 * @oddjob.property stateOperator
	 * @oddjob.description Set the way the children's state is 
	 * evaluated and reflected by the parent. Values can be WORST, 
	 * ACTIVE, or SERVICES.
	 * @oddjob.required No, default is ACTIVE.
	 * 
	 * @param stateOperator The state operator to be applied to children's
	 * states to derive our state.
	 */
	@ArooaAttribute
	public void setStateOperator(StateOperator stateOperator) {
		this.structuralState.setStateOperator(stateOperator);
	}
	
	/**
	 * Getter for State Operator.
	 * 
	 * @return
	 */
	public StateOperator getStateOperator() {
		return this.structuralState.getStateOperator();
	}
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}

	@Override
	public boolean isJoin() {
		return join;
	}

	public void setJoin(boolean join) {
		this.join = join;
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
