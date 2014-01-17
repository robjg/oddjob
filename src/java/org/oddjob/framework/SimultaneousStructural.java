package org.oddjob.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
abstract public class SimultaneousStructural extends StructuralJob<Object> 
implements Stoppable {

	private static final long serialVersionUID = 2009031800L;
	
	/** The executor to use. */
	private volatile transient ExecutorService executorService;

	/** The job threads. */
	private volatile transient List<Future<?>> futures;
	
	/** Watch execution to start the state reflector when all children
	 * have finished. */
	private transient volatile ExecutionWatcher executionWatcher;
	
	/**
	 * Create a new instance.
	 */
	public SimultaneousStructural() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		executionWatcher = 
				new ExecutionWatcher(new Runnable() {
					public void run() {
						stop = false;
						SimultaneousStructural.super.startChildStateReflector();
					}
			});		
	}
	
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
	
	public ExecutorService getExecutorService() {
		return executorService;
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

	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	protected void execute() throws InterruptedException, ExecutionException {
		if (executorService == null) {
			throw new NullPointerException("No Executor! Were services set?");
		}
		
		// it's possible to reset children and then execute again so this
		// is just in case there was no reset.
		childStateReflector.stop();
		
		executionWatcher.reset();
		
		futures = new ArrayList<Future<?>>();
		
		for (Object child : childHelper) {
			if (stop) {
				break;
			}
			
			if (!(child instanceof Runnable)) {
				logger().info("Child [" + child + 
						"] is not Runnable - ignoring.");
				continue;
			}

			Runnable job = (Runnable) child;
			
			Future<?> future = executorService.submit(
					executionWatcher.addJob(job));
			futures.add(future);
			
			logger().info("Submitted [" + job + "]");
		}
		
		if (stop) {
			return;
		}
		
		if (isJoin()) {
			logger().info("Join property is set, waiting for threads to finish.");
			for (Future<?> future : futures) {
				future.get();
			}
		}		
		else {
			if (futures.size() > 0) {
				stateHandler.waitToWhen(new IsStoppable(), new Runnable() {
					public void run() {
						getStateChanger().setState(ParentState.ACTIVE);
					}
				});
			}
		}
		executionWatcher.start();
	}

	@Override
	protected void onStop() throws FailedToStopException {
		super.onStop();

		Iterable<Future<?>> jobThreads = this.futures;
		if (jobThreads == null) {
			return;
		}

		for (Future<?> future : jobThreads) {
			future.cancel(false);
		}
		
		executionWatcher.stop();
	}
	
	@Override
	protected void startChildStateReflector() {
		// This is started by us so override and do nothing.
	}
	
	public boolean isJoin() {
		return false;
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
