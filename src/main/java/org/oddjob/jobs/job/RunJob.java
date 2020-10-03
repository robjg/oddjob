package org.oddjob.jobs.job;

import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.ComponentBoundary;
import org.oddjob.state.*;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.OddjobConfigException;
import org.oddjob.util.Restore;

import java.util.Date;

/**
 * @oddjob.description A job which runs another job. The other job can be
 * local or on a server.
 * <p>
 * This job reflects the state of the job being executed.
 * <p>
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>The {@link org.oddjob.jmx.JMXClientJob} job has an
 *  example that uses <code>run</code> to run a job on a 
 *  remote server.</li>
 * </ul>
 *  
 * 
 * @author Rob Gordon
 */
public class RunJob extends StructuralJob<Object>
implements Structural, Stoppable {
    private static final long serialVersionUID = 20050806201204300L;

	/** 
	 * @oddjob.property
	 * @oddjob.description Job to run
	 * @oddjob.required Yes.
	 */
	private volatile transient Object job;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The reset level. See {@link ResetActions}. 
	 * @oddjob.required No, defaults to NONE.
	 */
	private volatile transient ResetAction reset;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Wait for the target job to finish executing. 
	 * @oddjob.required No, defaults to false.
	 */
	private volatile boolean join;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Add the target job as a child of this job. Allows
	 * the target job to be easily monitored from a UI.
	 * @oddjob.required No, defaults to false.
	 */
	private volatile boolean showJob;
	
	/** Helper to ensure consistent states. */
	private volatile AsyncJobWait jobWait = new AsyncJobWait() {
		private static final long serialVersionUID = 2015041600L;
		protected void childDestroyed() {
			childHelper.removeAllChildren();
			super.childDestroyed();
		}
	};
	
	/**
	 *  Our state operator must cope with a client node
	 * that has been destroyed because the client has
	 * been stopped.
	 */
	class BespokeStateOperator extends DestroyHandlingStateOp {
		
		public BespokeStateOperator(StateOperator delegate) {
			super(delegate);
		}
		
		@Override
		protected StateEvent onDestroyed(int index) {
			try (Restore ignored = ComponentBoundary.push(loggerName(), RunJob.this)) {
				stopChildStateReflector();
				childHelper.removeAllChildren();
				stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						if (stateHandler().lastStateEvent().getState().isStoppable()) {
							logger().info("Job Destroyed while active, setting state to COMPLETE");
							getStateChanger().setStateException(
									new RuntimeException("Child Job has been destroyed."));
						}
						else {
							logger().info("Job Destroyed, leaving job in previous state.");
						}
					}
				});
			}
			// This will be unused as we've stopped the child state reflector.
			return new StateEvent(RunJob.this, ParentState.EXCEPTION, new Date(),
					new RuntimeException("Child Destroyed"));
		}
	}
	
	/**
	 * Set the stop node directly.
	 * 
	 * @param node The job.
	 */
	@ArooaAttribute
	synchronized public void setJob(Object node) {
		this.job = node;
	}

	/**
	 * Get the job.
	 * 
	 * @return The node.
	 */
	synchronized public Object getJob() {
		return this.job;
	}	
	
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
		this.structuralState.setStateOperator(
				new BespokeStateOperator(stateOperator));
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
		
		return new BespokeStateOperator(new AnyActiveStateOp());
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws Exception {
		
		if (job == null) {
			throw new OddjobConfigException("A job to start must be provided.");
		}
		
		Object proxy;
		if (childHelper.size() == 0) {
			ComponentProxyResolver resolver = 
					getArooaSession().getComponentProxyResolver();
			
			if (resolver == null) {
				throw new NullPointerException("No Component Proxy Resolver in Session.");
			}
			
			proxy = resolver.resolve(job, getArooaSession());
			
			// if we created the proxy we need to set the session.
			// This is required for reset actions that might use descriptor 
			// annotations. We don't set context and so don't have any
			// way to destroy the proxy. This might need to be addressed
			// at a later date.
			if (proxy != job && proxy instanceof ArooaSessionAware) {
				((ArooaSessionAware) proxy).setArooaSession(
						getArooaSession());
			}
			
			// OddjobComponentResolver should ensure this, so this is 
			// just a sanity check.
			if (!(proxy instanceof Stateful)) {
				throw new IllegalStateException("Resolved Proxy is not Stateful.");
			}
			if (!(proxy instanceof Runnable)) {
				throw new IllegalStateException("Resolved Proxy is not Runnable.");
			}
			
			childHelper.addChild(proxy);
		}
		else {
			proxy = childHelper.getChild();
		}
		
		ResetAction reset = this.reset;
		if (reset == null) {
			reset = ResetActions.NONE;
		}
		reset.doWith(proxy);		
			
		jobWait.setJoin(isJoin());
		boolean asynchronous = jobWait.runAndWaitWith((Runnable) proxy);
		
		// Ensure an asynchronous job always goes to active for the benefit
		// of consistent state transitions even if it is already complete.
		if (asynchronous) {
				stateHandler().waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ParentState.ACTIVE);
				}
			});
		}
	}

	/**
	 * Perform a soft reset on the job.
	 */
	public boolean softReset() {
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler().waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {
				
					logger().debug("Propagating Soft Reset to children.");			
					
					stopChildStateReflector();
					childHelper.removeAllChildren();
					stop = false;
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Soft Reset complete.");
				}
			});	
		} 
	}
	
	/**
	 * Perform a hard reset on the job.
	 */
	public boolean hardReset() {
		
		try (Restore restore = ComponentBoundary.push(loggerName(), this)) {
			return stateHandler().waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {
					logger().debug("Propagating Hard Reset to children.");			
					
					stopChildStateReflector();
					childHelper.removeAllChildren();
					stop = false;
					getStateChanger().setState(ParentState.READY);
					
					logger().info("Hard Reset complete.");
				}
			});
		}
	}
	
	@Override
	protected void onStop() throws FailedToStopException {
		jobWait.stopWait();
	}
	
	@Override
	public void addStructuralListener(StructuralListener listener) {
		if (isShowJob()) {
			super.addStructuralListener(listener);
		}
	}
	
	public ResetAction getReset() {
		return reset;
	}

	@ArooaAttribute
	public void setReset(ResetAction reset) {
		this.reset = reset;
	}
	
	public void setJoin(boolean join) {
		this.join = join;
	}
	
	public boolean isJoin() {
		return join;
	}
	
	public void setShowJob(boolean showJob) {
		this.showJob = showJob;
	}
	
	public boolean isShowJob() {
		return showJob;
	}
}
