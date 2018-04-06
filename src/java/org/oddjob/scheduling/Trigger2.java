/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.scheduling;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;

import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.utils.Try;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.scheduling.state.TimerState;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.StateOperator;
import org.oddjob.util.Restore;

/**
 * @oddjob.description An Experimental Trigger that fires on evaluating a State 
 * Expression to True.
 * 
 * @oddjob.example
 * 
 * A trigger expression based on the state of some jobs.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TriggerExpressionExample.xml}
 * 
 * 
 * @author Rob Gordon.
 */
public class Trigger2 extends ScheduleBase {
	
	private static final long serialVersionUID = 2009031000L; 
	
	/**
	 * @oddjob.property
	 * @oddjob.description A State Expression that when true will cause the trigger
	 * to fire.
	 * @oddjob.required No.
	 */
	private Function<Consumer<Try<Boolean>>, Restore> trigger;
		
	/** The scheduler to schedule on. */
	private transient ExecutorService executors;

	/** The schedule id. */
	private volatile transient Future<?> future;
	
	private volatile transient Restore restore;
	
	@ArooaHidden
	@Inject
	public void setExecutorService(ExecutorService executor) {
		this.executors = executor;
	}
	
	@Override
	protected StateOperator getStateOp() {
		return new AnyActiveStateOp();
	}
	
	@Override
	protected void begin() {

		Objects.requireNonNull(trigger, "No Trigger.");

		restore = trigger.apply(result -> {
			if (result.orElse( e -> {
				stateHandler.waitToWhen(new IsAnyState(), 
						() -> getStateChanger().setStateException(e));					
				removeListener();
				return false;
			})) {
				// Only execute once.
				removeListener();
				future = executors.submit(new Execution());
			}
		});
	}
	
	@Override
	protected void onStop() {

		Optional.ofNullable(future)
		.map(future -> future.cancel(false));
		removeListener();
	}
	
	@Override
	protected void postStop() {
	    childStateReflector.start();
	}
	
	/**
	 * Remove the state listener from the job we're triggering on.
	 */
	private void removeListener() {

		Optional.ofNullable(restore).ifPresent(Restore::close);		
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
		
	public Function<Consumer<Try<Boolean>>, Restore> getTrigger() {
		return trigger;
	}

	public void setTrigger(Function<Consumer<Try<Boolean>>, Restore> trigger) {
		this.trigger = trigger;
	}

	/**
	 * Wrap the job. This is the Runnable that is submitted.
	 */
	class Execution implements Runnable {
		public void run() {
			
			try (Restore restore = ComponentBoundry.push(loggerName(), Trigger2.this)) {

				logger().info("Executing child.");

				stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
					@Override
					public void run() {
						getStateChanger().setState(TimerState.ACTIVE);
					}
				});
				
				Runnable job = childHelper.getChild();

				if (job == null) {
					logger().warn("Nothing to run. Job is null!");
				}
				else {
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
	
}
