package org.oddjob.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
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
 * cascade had been set up. This job will complete asynchronously once all
 * it's children have completed.
 * <p>
 * 
 * @oddjob.example
 * 
 * A cascade of two jobs.
 * 
 * <pre>
 * &lt;state:cascade xmlns:state="http://rgordon.co.uk/oddjob/state"&gt;
 *  &lt;jobs&gt;
 *   &lt;echo message="This runs first."&gt;/>
 *   &lt;echo message="Then this."&gt;/>
 *  &lt;/jobs&gt;
 * &lt;/state:cascade&gt;
 * </pre>
 * 
 * @author Rob Gordon
 */

public class CascadeJob extends StructuralJob<Runnable> {
	
	private static final long serialVersionUID = 2010081100L;
	
	/** The executor to use. */
	private volatile transient ExecutorService executors;

	private ChildHelper<Runnable> actualChildren;
	
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
	public void setJobs(int index, Runnable child) {
	    logger().debug(
	    		"Adding child [" + 
	    		child + "], index [" + 
	    		index + "]");
	    
		if (child == null) {
			childHelper.removeChildAt(index);
			actualChildren.removeChildAt(index);
			if (index == 0 && childHelper.size() > 0) {
				Trigger trigger = (Trigger) childHelper.removeChildAt(0);
				trigger.destroy();
				childHelper.insertChild(0, actualChildren.getChildAt(0));
			}
		}
		else {
			if (!(child instanceof Stateful)) {
				throw new IllegalArgumentException("Children must be Stateful.");
			}
			
			actualChildren.insertChild(index, child);
			if (index == 0) {
				childHelper.insertChild(index, child);
			}
			else {
				Trigger trigger = new Trigger();
				trigger.setOn((Stateful) childHelper.getChildAt(index -1));
				trigger.setJob(child);
				trigger.setExecutorService(executors);
				
				childHelper.insertChild(index, trigger);
			}
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
		
		Runnable[] children = childHelper.getChildren(new Runnable[0]);
		
		for (int i = 0; i < children.length; ++i) {
			children[i].run();
		}		
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
	protected void onConfigured() {
		if (executors == null) {
			throw new NullPointerException("No ExecutorsService.");
		}
		
		Object[] children = childHelper.getChildren(
				new Runnable[childHelper.size()]);
		for (int i = 1; i < children.length; ++i) {
			((Trigger) children[i]).setExecutorService(executors);
		}
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
