package org.oddjob.jobs.structural;


import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;
import org.oddjob.state.SequentialHelper;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;

/**
 * @oddjob.description Executes it's children in a sequence. The sequence 
 * will only continue to be executed if each child completes. 
 * If a child is incomplete, or flags an exception then execution
 * will terminate and this job's state will reflect that of the 
 * failed child.
 * <p>
 * If the failed job is later run manually and completes this Job will
 * reflect the new state. As such it is useful as a trigger point for 
 * the completion of a sequence of jobs.
 * 
 * @oddjob.example
 * 
 * A simple sequence of two jobs.
 * 
 * <pre>
 * &lt;sequential id="trigger-on-me" name="A sequence of two jobs"&gt;
 *  &lt;jobs&gt;
 *   &lt;echo text="This runs first" /&gt;
 *   &lt;echo text="This runs after" /&gt;
 *  &lt;/jobs&gt;
 * &lt;/sequential&gt;
 * <pre>
 * 
 * 
 * @author Rob Gordon 
 */

public class SequentialJob extends StructuralJob<Object>
			implements Structural, Stoppable {
	private static final long serialVersionUID = 20051206;
	
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
	
	/**
	 * Add a child.
	 * 
	 * @oddjob.property jobs
	 * @oddjob.description The child jobs.
	 * @oddjob.required No, but pointless if missing.
	 * 
	 * @param child A child
	 */
	@ArooaComponent
	public void setJobs(int index, Object child) {
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
	public void execute() throws Exception {		
		Object[] children = childHelper.getChildren(); 
		
		for (int i = 0; i < children.length && !stop; ++i) {
			
			Object child = children[i];
			if (!(child instanceof Runnable)) {
				logger().info("Not Executing [" + child + "] as it is not a job.");
			}
			else {
				Runnable job = (Runnable) child;
				logger().info("Executing child [" + job + "]");
				
				job.run();
			}
			
			// Test we can still continue children.
			if (!new SequentialHelper().canContinueAfter(child)) {						
				logger().info("Child [" + child + "] failed. Can't continue.");
				break;
			}
		}
	}	
}
