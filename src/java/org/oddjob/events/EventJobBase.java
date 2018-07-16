/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.framework.util.AsyncExecutionSupport;
import org.oddjob.framework.util.ComponentBoundry;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;
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
abstract public class EventJobBase<T> extends StructuralJob<Object> {
	
	private static final long serialVersionUID = 2018060600L; 
	
	/** Watch execution to start the state reflector when all children
	 * have finished, and track job threads.  */
	private volatile transient AsyncExecutionSupport asyncSupport;	
		
	/** The scheduler to schedule on. */
	private volatile transient ExecutorService executorService;

	private volatile Object current;

	private volatile transient Restore restore;
	
	public EventJobBase() {
		completeConstruction();
	}
	
	/**
	 * Called once following construction or deserialisation.
	 */
	private void completeConstruction() {
		asyncSupport = 
				new AsyncExecutionSupport(new Runnable() {
					public void run() {
						stop = false;
						try {
							save();
							EventJobBase.super.startChildStateReflector();
						} catch (ComponentPersistException e) {
							stateHandler().waitToWhen(s -> true, 
									() -> getStateChanger().setStateException(e));
							onStop();
						}
					}
			});		
	}

	@ArooaHidden
	@Inject
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}
	
	@Override
	protected void startChildStateReflector() {
		// we start this ourselves
	}
	
	abstract protected SwitchoverStrategy getSwitchoverStrategy();


	@Override
	protected void execute() throws Throwable {
		Object[] children = childHelper.getChildren();
		
		if (children.length < 1) {
			throw new IllegalArgumentException("No When Node.");
		}
		
		asyncSupport.reset();

		@SuppressWarnings("unchecked")
		SubscribeNode<? super T> when = (SubscribeNode<? super T>) children[0];
		
		Object job;
		if (children.length > 1) {
			job = children[1];			
		}
		else {
			job = null;
		}
		
		Restore close = when.start(
				v -> {
					try (Restore restore = ComponentBoundry.push(loggerName(), EventJobBase.this)) {
						stopChildStateReflector();
						getSwitchoverStrategy().switchover(
							() -> current = v, 
							job,
							j -> {
								asyncSupport.submitJob(executorService, j);
								asyncSupport.startWatchingJobs();
								logger().info("Submitted [" + j + "]");
	
							});
					}
				});		
				
		if (job == null) {
			EventJobBase.super.startChildStateReflector();
		}
		else {
			stateHandler().waitToWhen(new IsStoppable(), 
					() -> getStateChanger().setState(ParentState.ACTIVE));
		}

		restore = () -> {
			close.close();
			restore = null;
			
		};
	}
	
	@Override
	protected void onStop() {

		asyncSupport.cancelAllPendingJobs();
		Optional.ofNullable(restore).ifPresent(Restore::close);		
	}
	
//	@Override
//	protected void postStop() {
//	    childStateReflector.start();
//	}
				
	public Object getCurrent() {
		return current;
	}
	
	/**
	 * Add a child job.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setJobs(int index, Object child) {
	    logger().debug(
	    		"Adding child [" + 
	    		child + "], index [" + 
	    		index + "]");
	    
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}

	
	public interface SwitchoverStrategy {
	
		void switchover(Runnable changeValues, Object job, Executor exector);
	}	
	
	/*
	 * Custom serialization.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}
	
	/*
	 * Custom serialization.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}
}
