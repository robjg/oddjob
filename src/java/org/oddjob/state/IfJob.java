package org.oddjob.state;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;

/**
 * @oddjob.description
 * 
 * This job implements an if/then/else logic. This job can contain
 * any number of child jobs. The first job is taken to be the condition.
 * If the resulting state matches the given state the second job is
 * executed. If it doesn't then the third job is executed, (if it exists).
 * <p>
 * The completion state is that of the then or else job. If either don't 
 * exist then the Job is flagged as complete.
 * <p>
 * If any more than three jobs are provided the extra jobs are ignored.
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
 * @author Rob Gordon
 */


public class IfJob extends StructuralJob<Object>
		implements Runnable, Stateful, Resetable, Structural, Stoppable {
    private static final long serialVersionUID = 20050806;
    
    /** The condition state. */
	private StateCondition state = StateConditions.COMPLETE;
	
	/** remember then/else */ 
	private volatile boolean then;
	
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
	 * @oddjob.description The state to check against.
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
	public void setJobs(int index, Runnable job) {
	    if (job == null) {
	    	childHelper.removeChildAt(index);
	    }
	    else {
	    	childHelper.insertChild(index, job);
	    }
	}
		
	@Override
	protected StateOperator getStateOp() {
		return new StateOperator() {
			public ParentState evaluate(State... states) {

				if (states.length < 1) {
					return ParentState.READY;
				}
				
				then =  state.test(states[0]);
				
				if (then) {
					if (states.length > 1) {
						return new ParentStateConverter().toStructuralState(states[1]);
					}
				}
				else {
					if (states.length > 2) {
						return new ParentStateConverter().toStructuralState(states[2]);
					}
				}
				
				return ParentState.COMPLETE;
			}
		};

	}
	
	protected void execute() {
		
		if (childHelper.size() < 1) {
			return;
		}
		
		Stateful depends = (Stateful) childHelper.getChildAt(0);
		
		((Runnable) depends).run();
		
		if (stop) {
			stop = false;
			return;
		}
		
		if (then) {
			
			if (childHelper.size() < 2) {
				return;
			}
			
			Runnable job = (Runnable) childHelper.getChildAt(1); 
			
	    	job.run();
		}
		else {
			
			if (childHelper.size() < 3) {
				return;
			}
			
			Runnable job = (Runnable) childHelper.getChildAt(2); 
			
	    	job.run();
		}
	}
	
}

