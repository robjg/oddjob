package org.oddjob.state;

import org.oddjob.*;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.registry.ServiceFinder;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.AsyncExecutionSupport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @oddjob.description
 * 
 * This job implements an if/then/else logic based on job state. This job can 
 * contain any number of child jobs. The first provides the state for
 * the condition.
 * If this state matches the given state, the second job is
 * executed. If it doesn't, then the third job is executed, (if it exists).
 * <p>
 * The completion state is that of the then or else job. If either don't 
 * exist then the Job is flagged as complete.
 * <p>
 * If any more than three jobs are provided the extra jobs are ignored.
 * <p>
 * If the first job enters an ACTIVE state then condition will not be
 * evaluated until the first job leaves the ACTIVE state. This job will
 * not block while this is happening. The thread of execution will pass
 * to its next sibling and this job will also enter the ACTIVE state.
 * 
 * @oddjob.example
 * 
 * If a file exists. 
 * 
 * {@oddjob.xml.resource org/oddjob/state/IfFileExistsExample.xml}
 * 
 * @oddjob.example
 * 
 * An example showing lots of if's. All these if's go to COMPLETE state 
 * when run.
 * 
 * {@oddjob.xml.resource org/oddjob/state/if.xml}
 * 
 * @oddjob.example
 * 
 * Asynchronous evaluation. Only when the first job moves beyond it's ACTIVE
 * state will the condition be evaluated and the then job (second job) 
 * be executed. The execution of the second job is also asynchronous.
 * 
 * {@oddjob.xml.resource org/oddjob/state/IfJobAsyncThen.xml}
 * 
 * @author Rob Gordon
 */
public class IfJob extends StructuralJob<Object>
		implements Runnable, Stateful, Resettable, Structural, Stoppable {
	
    private static final long serialVersionUID = 20050806;
    
    /** The condition state. */
	private StateCondition state = StateConditions.COMPLETE;
	
	/** @oddjob.property
	 *  @oddjob.description Used for an asynchronous evaluation of the if. 
	 *  @oddjob.required No. Will be provided by the framework.
	 */
	private volatile transient ExecutorService executorService;
	
	/** Used to find Executor Service if none provided. */
	private volatile transient ServiceFinder serviceFinder;
	
	/** Support asynchronous ifs. */
	private volatile transient AsyncExecutionSupport asyncSupport;	

	@Override
	@ArooaHidden
	public void setArooaContext(ArooaContext context) {
		super.setArooaContext(context);
		
		serviceFinder = context.getSession().getTools(
				).getServiceHelper().serviceFinderFor(context);
	}
	
	/**
	 * Getter for state.
	 * 
	 * @return The state.
	 */
	public StateCondition getState() {
		return state;
	}
	
	/**
	 * @oddjob.property state
	 * @oddjob.description The state condition to check against. 
	 * See the Oddjob User guide for a full list of state conditions.
	 * @oddjob.required No, defaults to COMPLETE.
	 */
	@ArooaAttribute
	public void setState(StateCondition state) {
		this.state = state;
	}		
	
	/**
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required At least one.
	 */
	@ArooaComponent
	public void setJobs(int index, Object job) {
	    if (job == null) {
	    	childHelper.removeChildAt(index);
	    }
	    else {
	    	childHelper.insertChild(index, job);
	    }
	}
		
	@Override
	protected StateOperator getInitialStateOp() {
		return states -> {

			if (states.length == 0) {
				return null;
			}

			boolean then = state.test(states[0].getState());

			if (then) {
				if (states.length > 1) {
					return StateOperator.toParentEvent(states[1],
							new StandardParentStateConverter());
				}
			}
			else {
				if (states.length > 2) {
					return StateOperator.toParentEvent(states[2],
							new StandardParentStateConverter());
				}
			}

			return new StateEvent(IfJob.this, ParentState.COMPLETE);
		};

	}
	
	protected void execute() {
		
		if (childHelper.size() < 1) {
			return;
		}

		Object child = childHelper.getChildAt(0);
		if (!(child instanceof Stateful)) {
			logger().info("Child [" + child + 
					"] is not Stateful - ignoring.");
			return;
		}
		if (!(child instanceof Runnable)) {
			logger().info("Child [" + child + 
					"] is not Runnable - ignoring.");
			return;
		}
		
		final Stateful depends = (Stateful) child;
		
		final class ThenAction implements Runnable {
			
			@Override
			public void run() {
				if (childHelper.size() < 2) {

					logger().info("No job for then.");
				}
				else {
					
					logger().info("Running job for then.");
					
					Runnable job = (Runnable) childHelper.getChildAt(1); 
					
				    job.run();
				}
			}
		}
			
		class ElseAction implements Runnable {
			@Override
			public void run() {
				if (childHelper.size() < 3) {

					logger().info("No job for else.");
				}
				else {
					logger().info("Running job for else.");
	
					Runnable job = (Runnable) childHelper.getChildAt(2); 
					
			    	job.run();		    	
				}
			}
		}
			
		class AsyncAction implements Runnable {
			@Override
			public void run() {
				
				asyncSupport = new AsyncExecutionSupport(() -> {
					stop = false;
					IfJob.super.startChildStateReflector();
				});
				
				stateHandler().waitToWhen(new IsAnyState(),
						() -> getStateChanger().setState(ParentState.ACTIVE));
				
				depends.addStateListener(new StateListener() {
					
					@Override
					public void jobStateChange(StateEvent event) {
						
						State dependsState = event.getState();
						
						StateCondition condition = StateConditions.LIVE;
						
						if (condition.test(dependsState)) {
							return;
						}
						
						ExecutorService executorService = 
								ensureExecutorService();
						
						if (!stop) {
							if (IfJob.this.state.test(dependsState)) {

								logger().info("State of [" + dependsState +
										"], triggering 'then' action.");

								asyncSupport.submitJob(executorService,
										new ThenAction());
							}
							else {

								logger().info("State of [" + dependsState +
										"], triggering 'else' action.");

								asyncSupport.submitJob(executorService,
										new ElseAction());
							}
						}
						
						depends.removeStateListener(this);
						
						asyncSupport.startWatchingJobs();
					}
				});				
			}
		}
		
		final AtomicReference<Runnable> action =
				new AtomicReference<>(() -> {

					State dependsState =
							depends.lastStateEvent().getState();

					if (state.test(dependsState)) {

						new ThenAction().run();
					} else {

						new ElseAction().run();
					}
				});
		
		StateListener listenForActive = event -> {

			StateCondition condition = StateConditions.ACTIVE;

			if (condition.test(event.getState())) {

				logger().info("Setting asynchronous mode.");

				action.set(new AsyncAction());
			}
		};
		
		depends.addStateListener(listenForActive);
		
		try {
			((Runnable) depends).run();
		}
		finally {
			depends.removeStateListener(listenForActive);
		}
						
		if (stop) {
			stop = false;
			return;
		}

		action.get().run();		
	}

	@Override
	protected void onStop() throws FailedToStopException {
		super.onStop();
		
		if (asyncSupport != null) {
			asyncSupport.cancelAllPendingJobs();
		}
	}
	
	@Override
	protected void startChildStateReflector() {
		// In asynchronous mode the child reflector is only started when all
		// the jobs complete.
		if (asyncSupport == null) {
			super.startChildStateReflector();
		}
	}
	
	protected ExecutorService ensureExecutorService() {
		
		if (executorService == null) {
			
			if (serviceFinder == null) {
				throw new IllegalStateException(
						"No Service Finder - Need to set An Arooa Context.");
			}
			
			executorService = serviceFinder.find(ExecutorService.class, null);
			
			if (executorService == null) {
				throw new IllegalStateException("No Executor Service");
			}
		}
		
		return executorService;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
}

