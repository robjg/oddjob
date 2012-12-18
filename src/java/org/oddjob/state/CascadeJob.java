package org.oddjob.state;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.framework.ExecutionWatcher;
import org.oddjob.framework.StructuralJob;
import org.oddjob.jobs.structural.ParallelJob;
import org.oddjob.jobs.structural.SequentialJob;

/**
 * @oddjob.description
 * 
 * A job which triggers the next job after the previous one completes.
 * This job differs from a {@link SequentialJob} task in that the latter
 * follows the thread of execution, and only checks state to ensure
 * it can continue. This job's thread of execution passes onwards after the
 * cascade has been set up. This job will complete asynchronously once all
 * it's children have completed.
 * 
 * <h4>State Operator</h4>
 * This job doesn't currently expose a State Operator property as 
 * {@link SequentialJob} does. The state behaviour is equivalent to the
 * WORST state operator, which is what is desired in most situations. A
 * <code>stateOperator</code> property may be added in future versions
 * if needed.
 * 
 * @oddjob.example
 * 
 * A cascade of two jobs.
 * 
 * {@oddjob.xml.resource org/oddjob/state/CascadeExample.xml}
 * 
 * @oddjob.example
 * 
 * Showing cascade being used with {@link ParallelJob}. The cascade will
 * wait for the parallel job to finish before running the third job.
 * 
 * {@oddjob.xml.resource org/oddjob/state/CascadeWithParallelExample.xml}
 * 
 * @author Rob Gordon
 */
public class CascadeJob extends StructuralJob<Object> {
	
	private static final long serialVersionUID = 2010081100L;
	
	/** The executor to use. */
	private volatile transient ExecutorService executors;

	/** The first job. */
	private transient Future<?> future;
	
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
	public synchronized void setJobs(int index, Object child) {
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			if (child instanceof Runnable ^ child instanceof Stateful) {
				throw new IllegalArgumentException(
					"If a job is Runnable or Stateful then it must be both.");
			}
			childHelper.insertChild(index, child);
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
		
		final Iterator<Object> children = childHelper.iterator();
				
		final ExecutionWatcher executionWatcher = 
			new ExecutionWatcher(new Runnable() {
				public void run() {
					stop = false;
					CascadeJob.super.startChildStateReflector();
				}
		});
		
		// Used to flag the first execution. This is  used
		// to set the state to active.
		final AtomicBoolean first = new AtomicBoolean(true);
		
		// This runnable is run each time the state of the currently
		// started child isDone.
		new Runnable() {
			@Override
			public void run() {
				
				// Find the next runnable, ignoring folders.
				Runnable next = null;
				while (children.hasNext()) {
					Object child = children.next();
					if (child instanceof Runnable) {
						next = (Runnable) child;
						break;
					}
				}
				
				if (next == null || stop) {
					executionWatcher.start();
					return;
				}

				final Runnable _this = this;
				((Stateful) next).addStateListener(new StateListener() {
					public void jobStateChange(StateEvent event) {
						if (!new IsDoneOrCrashed().test(event.getState())) {
							return;
						}
						event.getSource().removeStateListener(this);
						if (event.getState().isDone()) {
							_this.run();
						}
						else {
							executionWatcher.start();
						}
					}
				});				
				Runnable wrapper = executionWatcher.addJob(next);
				
				if (first.get()) {
					first.set(false);
					stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
						public void run() {
							getStateChanger().setState(ParentState.ACTIVE);
						}
					});					
				}
				future = executors.submit(wrapper);
			}
		}.run();
			
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
	protected StateOperator getInitialStateOp() {
		return new WorstStateOp();
	}
			
	@Override
	protected void startChildStateReflector() {
		// This is started by us so override and do nothing.
	}

}
