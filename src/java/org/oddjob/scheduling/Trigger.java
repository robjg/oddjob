/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.images.IconHelper;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;

/**
 * @oddjob.description A trigger runs it's job when the job being triggered
 * on enters the state specified.
 * <p>
 * Once the trigger's job runs the trigger
 * will reflect the state of the it's job. The trigger will continue to 
 * reflect it's job's state until it is reset. 
 * <p>Subsequent state changes in
 * the triggering job are ignored until the trigger is reset and re-run.
 * <p>
 * If the triggering job is destroyed, because it is deleted or on a remote
 * server the trigger will enter an exception state.
 * <p>
 * 
 * @oddjob.example
 * 
 * A trigger that runs once two other jobs have completed.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TriggerExample.xml}
 * 
 * @oddjob.example
 * 
 * Examples Elsewhere.
 * <p>
 * The scheduling example uses a trigger to send an email if one
 * of the transfers fail.
 * 
 * @author Rob Gordon.
 */
public class Trigger extends ScheduleBase {
	
	private static final long serialVersionUID = 2009031000L; 
	
	/**
	 * @oddjob.property
	 * @oddjob.description The job the trigger will trigger on.
	 * @oddjob.required Yes.
	 */
	private transient Stateful on;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The state which will cause the trigger
	 * to fire.
	 * @oddjob.required No, defaults to COMPLETE.
	 */
	private StateCondition state = StateConditions.COMPLETE;
	
	/** The last time the trigger fired. */
	private Date lastTime;
	
	/** The scheduler to schedule on. */
	private transient ExecutorService executors;

	/** The schedule id. */
	private transient Future<?> future;
	
	private transient StateListener listener;
	
	@ArooaHidden
	@Inject
	public void setExecutorService(ExecutorService executor) {
		this.executors = executor;
	}
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
	
	@Override
	protected void begin() throws Throwable {
			if (on == null) {
				throw new NullPointerException("Nothing to trigger on.");
			}
			if (executors == null) {
				throw new NullPointerException("No ExecutorService.");
			}
			
			listener = 
				new StateListener() {

				@Override
				public synchronized void jobStateChange(StateEvent event) {
					logger().debug("Trigger on [" + on + "] has state [" + 
							event.getState() + "] + at " +
							event.getTime());
					
					if (event.getState().isDestroyed()) {
						stateHandler().waitToWhen(new IsStoppable(), 
								new Runnable() {
							@Override
							public void run() {
								getStateChanger().setStateException(
										new JobDestroyedException(on));
							}
						});
						on = null;
					}
						
					if (!state.test(event.getState())) {
						logger().debug("Not the trigger state, returning.");
						return;
					}
					
					// don't fire if event time hasn't changed.
					if (event.getTime().equals(lastTime)) {
						logger().info("Already had event for time " +
								event.getTime() + ", not triggering.");
						return;
					}
					
					lastTime = event.getTime();
					
					// We won't fire again until run again.
					removeListener();
					
					logger().debug("[" + Trigger.this + "] submitting [" + 
							childHelper.getChild() + "] for immediate execution.");

					future = executors.submit(new Execution());
				};
			};
			
			on.addStateListener(listener);
			
			iconHelper.changeIcon(IconHelper.SLEEPING);
	
			logger().info("Wating for [" + on + "] to have state [" +
					state + "]");
	}
	
	@Override
	protected void onStop() {
		
		Future<?> future = null;
		synchronized (this) {
			future = this.future;
			this.future = null;
		}
		
		if (future != null) {
			future.cancel(true);
		}
		
		removeListener();
	}
	
	private void removeListener() {

		StateListener listener = null;
		
		synchronized (this) {
			listener = this.listener;
			this.listener = null;
		}
		if (listener != null) {
			on.removeStateListener(listener);
		}
	}
	
	/**
	 * @oddjob.property job
	 * @oddjob.description The job to run when the trigger fires.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public synchronized void setJob(Runnable job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}
		
	public StateCondition getState() {
		return state;
	}
	
	@ArooaAttribute
	public void setState(StateCondition state) {
		this.state = state;
	}
		
	
	public Stateful getOn() {
		return on;
	}

	@ArooaAttribute
	public void setOn(Stateful triggerOn) {
		this.on = triggerOn;
	}

	/**
	 */
	class Execution implements Runnable {
		public void run() {
			
		    logger().debug("[" + Trigger.this + 
		    		"] Executing child at [" + new Date()+ "]");

		    // check job state here because it guarantees all other
		    // state listeners have been notified.
		    on.lastStateEvent();
			
			iconHelper.changeIcon(IconHelper.EXECUTING);
		    
		    Runnable job = childHelper.getChild();
		    
		    if (job != null) {
		    
		    	// Note reset isn't necessary because this is a
		    	// single execution only.
		    	
				try {
					job.run();
					save();
				}
				catch (Throwable t) {
					logger().error("Failed running triggered job.", t);
				}
		    }
		    
		    childStateReflector.start();
		}
	}
		
}
