/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs.structural;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.StructuralJob;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.State;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;
import org.oddjob.structural.OddjobChildException;

/**
 * @oddjob.description This job will repeatedly either for a number of 
 * times or until the until property is true; 
 * <p>
 * Without either a until or a times the job will loop indefinitely.
 * 
 * @oddjob.example
 * 
 * Repeat a job 3 times.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/RepeatExample.xml}
 * 
 * @author Rob Gordon.
 * 
 */
public class RepeatJob extends StructuralJob<Runnable>
implements Stoppable {
	private static final long serialVersionUID = 20120121;
	
    /**
     * @oddjob.property 
     * @oddjob.description Repeat will repeat until the value of
     * this property is true.
     * @oddjob.required No.
     */
    private boolean until;
    
    /**
     * @oddjob.property 
     * @oddjob.description The count of repeats.
     * @oddjob.required Read Only.
     */
	private int count;
	
	/**
     * @oddjob.property 
     * @oddjob.description The number of times to repeat.
     * @oddjob.required No.
	 */
	private int times;
    
    
	@Override
	protected StateOperator getStateOp() {
		return new WorstStateOp();
	}
	
	/** 
	 * @oddjob.property job
	 * @oddjob.description The job who's execution 
	 * to schedule. 
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public void setJob(Runnable child) {
		if (child == null) {
			childHelper.removeChildAt(0);
		}
		else {
			if (childHelper.size() > 0) {
				throw new IllegalArgumentException("Child Job already set.");
			}
			childHelper.insertChild(0, child);
		}
	}
	

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jobs.AbstractJob#execute()
     */
	protected void execute() {
		
		Runnable job = childHelper.getChild(); 
		if (job == null) {
			return;
		}
		
		while (!stop && !until && (times == 0 || count < times)) {
		    ++count;
		    
			boolean softReset = false;
			if (job instanceof Stateful && 
					new IsSoftResetable().test(
							((Stateful) job).lastStateEvent().getState())) {
				softReset = true;
			}
			
			if (job instanceof Resetable) {
			    if (softReset) {
					((Resetable) job).softReset();
				}
				else {
					((Resetable) job).hardReset();
				}
	        }
	        
			try {
				job.run();
			}
			finally {
			}
			
			State state = null;
			Throwable throwable = null;
			if (job instanceof Stateful) {
				state = ((Stateful) job).lastStateEvent().getState();
				throwable = ((Stateful) job).lastStateEvent().getException();
			}
			
			if (state == null) {
				continue;
			}
			
			if (state.isException()) {
				logger().debug("Job [" + job + "] Exception");
				throw new OddjobChildException(throwable, job.toString());			
			}
			else if (new IsStoppable().test(state)) {
				logger().debug("Job state for [" + job + 
						"] is: " + state + ", Will not repeat.");
				break;
			}
		} // end while
		
	}

	@Override
	protected void onReset() {
		count = 0;
		until = false;
	}
	
	public boolean isUntil() {
		return until;
	}

	public void setUntil(boolean until) {
		this.until = until;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getCount() {
		return count;
	}

}
