package org.oddjob.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.ParentState;

/**
 * An abstract base class for Structural jobs where all child jobs
 * can run simultaneously.
 * 
 * @author rob
 *
 */
abstract public class SimultaneousStructural extends StructuralJob<Runnable> 
implements Stoppable {

	private static final long serialVersionUID = 2009031800L;
	
	/** The executor to use. */
	private volatile transient ExecutorService executorService;

	/** The job threads. */
	private volatile transient List<Future<?>> jobThreads;
	
	/**
	 * Set the {@link ExecutorService}.
	 * 
	 * @oddjob.property executorService
	 * @oddjob.description The ExecutorService to use. This will 
	 * be automatically set by Oddjob.
	 * @oddjob.required No.
	 * 
	 * @param child A child
	 */
	@Inject
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
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
	public void setJobs(int index, Runnable child) {
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

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws InterruptedException {
		if (executorService == null) {
			throw new NullPointerException("No Executor! Were services set?");
		}
		
		Runnable[] children = childHelper.getChildren(new Runnable[0]);

		ExecutionWatcher executionWatcher = 
			new ExecutionWatcher(new Runnable() {
				public void run() {
					SimultaneousStructural.super.startChildStateReflector();
				}
		});
		
		jobThreads = new ArrayList<Future<?>>();
		
		for (int i = 0; i < children.length && !stop; ++i) {
			Future<?> future = executorService.submit(
					executionWatcher.addJob(children[i]));
			jobThreads.add(future);
		}
		
		if (stop) {
			stop = false;
		}
		else {
			stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
				public void run() {
					getStateChanger().setState(ParentState.ACTIVE);
				}
			});

			executionWatcher.start();
		}
	}

	@Override
	protected void onStop() throws FailedToStopException {
		super.onStop();

		Iterable<Future<?>> jobThreads = this.jobThreads;
		if (jobThreads == null) {
			return;
		}

		for (Future<?> future : jobThreads) {
			future.cancel(false);
		}
		
		super.startChildStateReflector();
	}
	
	@Override
	protected void startChildStateReflector() {
		// This is started by us so override and do nothing.
	}
	
}
