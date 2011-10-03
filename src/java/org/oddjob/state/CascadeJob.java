package org.oddjob.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.framework.ExecutionWatcher;
import org.oddjob.framework.StructuralJob;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.scheduling.Trigger;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * @oddjob.description
 * 
 * A job which triggers the next job after the previous one completes.
 * This job differs from a {@link SequentialJob} task in that the latter
 * follows the thread of execution, and only checks state to ensure
 * it can continue. This job's thread of execution passes onwards after the
 * cascade has been set up. This job will complete asynchronously once all
 * it's children have completed.
 * <p>
 * 
 * @oddjob.example
 * 
 * A cascade of two jobs.
 * 
 * {@oddjob.xml.resource org/oddjob/state/CascadeExample.xml}
 * 
 * @author Rob Gordon
 */

public class CascadeJob extends StructuralJob<Runnable> {
	
	private static final long serialVersionUID = 2010081100L;
	
	/** The executor to use. */
	private volatile transient ExecutorService executors;

	/** The actual jobs. Structural Jobs contain the triggers. */
	private transient ChildHelper<Runnable> actualChildren;
	
	/** The first job. */
	private transient Future<?> future;
	
	public CascadeJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		actualChildren = new ChildHelper<Runnable>(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.OddjobAware#setOddjobServices(org.oddjob.OddjobServices)
	 */
	@Inject
	@ArooaHidden
	public void setExecutorService(ExecutorService executor) {
		this.executors = executor;
	}
	
	/**
	 * Add a child job.
	 * 
	 * @oddjob.property <i>jobs</i>
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public synchronized void setJobs(int index, Runnable child) {
	    logger().debug(
	    		"Adding child [" + 
	    		child + "], index [" + 
	    		index + "]");
	    
		if (child == null) {
			Object maybeTrigger = childHelper.removeChildAt(index);
			if (index > 0) {
				((Trigger) maybeTrigger).setJob(null);
				((Trigger) maybeTrigger).destroy();
			}
			
			actualChildren.removeChildAt(index);
			
			if (childHelper.size() > index) {
				Trigger trigger = (Trigger) childHelper.removeChildAt(index);
				trigger.setJob(null);
				trigger.destroy();
	
				insertTrigger(index, actualChildren.getChildAt(index));
			}
		}
		else {
			if (!(child instanceof Stateful)) {
				throw new IllegalArgumentException("Children must be Stateful.");
			}
			
			actualChildren.insertChild(index, child);
			insertTrigger(index, child);
			
			// Cope with a paste into an existing sequence.
			if (actualChildren.size() > index + 1) {
				Object maybeTrigger = childHelper.removeChildAt(index + 1);
				
				if (index + 1 > 1) {
					Trigger trigger = (Trigger) maybeTrigger;
					trigger.setJob(null);
					trigger.destroy();
				}
				
				insertTrigger(index + 1, actualChildren.getChildAt(index + 1));				
			}
		}
	}
	
	/**
	 * Inserts a trigger for a child.
	 * 
	 * @param index
	 * @param child
	 */
	private final void insertTrigger(int index, Runnable child) {
		
		if (index == 0) {
			childHelper.insertChild(0, child);
		}
		else {
			Trigger trigger = new Trigger();
			trigger.setOn((Stateful) actualChildren.getChildAt(index -1));
			trigger.setJob(child);
			trigger.setExecutorService(executors);
			trigger.setName("Cascade trigger for " + child.toString());
			
			childHelper.insertChild(index, trigger);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws InterruptedException {
		if (executors == null) {
			throw new NullPointerException("No Executor! Were services set?");
		}
		
		final Runnable[] children = childHelper.getChildren(new Runnable[0]);
		
		for (int i = 1; i < children.length; ++i) {
			((Trigger) children[i]).setExecutorService(executors);
			children[i].run();
		}
		
		ExecutionWatcher executionWatcher = 
			new ExecutionWatcher(new Runnable() {
				public void run() {
					CascadeJob.super.startChildStateReflector();
				}
		});
		
		if (children.length > 0) {
			
			Runnable wrapper = executionWatcher.addJob(children[0]);
			
			future = executors.submit(wrapper);
			
		}		
		
		stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
			public void run() {
				getStateChanger().setState(ParentState.ACTIVE);
			}
		});

		executionWatcher.start();
	}		
	
	@Override
	protected void onStop() {
		
		Future<?> future = null;
		synchronized (this) {
			future = this.future;
			this.future = null;
		}
		
		if (future != null) {
			future.cancel(false);
		}
		
		super.startChildStateReflector();
	}
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
	
	@Override
	public void addStructuralListener(StructuralListener listener) {
		actualChildren.addStructuralListener(listener);
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		actualChildren.removeStructuralListener(listener);
	}
		
	@Override
	protected void startChildStateReflector() {
		// This is started by us so override and do nothing.
	}

	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}
}
