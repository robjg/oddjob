/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;

import org.oddjob.Resetable;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.state.CompleteOrNotOp;
import org.oddjob.state.State;
import org.oddjob.state.StateOperator;

/**
 * @oddjob.description Provides a simple timer for periodic or once only 
 * execution of the child job.
 * <p>
 * 
 * @oddjob.example
 * 
 * A Timer that runs at 10am each day, Monday to Friday.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TimerExample.xml}
 * 
 * @author Rob Gordon.
 */
public class Timer extends TimerBase {
	
	private static final long serialVersionUID = 2009091401L; 
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The interval in the normal schedule in which the
	 * scheduled job last completed. This is the interval that will be used
	 * to determine when the next normal schedule is due. It can only be 
	 * changed using the reSchedule property.
	 * @oddjob.required Read only.
	 */ 
	private ScheduleResult lastComplete;

	/**
	 * @oddjob.property 
	 * @oddjob.description Don't reschedule if the scheduled job doesn't
	 * complete.
	 * @oddjob.required No.
	 */ 
	private boolean haltOnFailure;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description Use the current time, not the last completed time
	 * to calculate when the job is next due.
	 * @oddjob.required No.
	 */ 
	private boolean skipMissedRuns;
		
	@Override
	protected StateOperator getStateOp() {
		return new CompleteOrNotOp();
	}
	
	@Override
	protected void begin() throws Throwable {

		super.begin();
		
		if (getCurrent() != null && !skipMissedRuns) {
			setNextDue(getCurrent().getFromDate());
		}
		else {
			scheduleFrom(getClock().getDate());
		}
	}
		
    public void setHaltOnFailure(boolean haltOnFailure) {
    	this.haltOnFailure = true;
    }
    
    public boolean isHaltOnFailure() {
    	return haltOnFailure;
    }
    		
	public boolean isSkipMissedRuns() {
		return skipMissedRuns;
	}

	public void setSkipMissedRuns(boolean skipMissedRuns) {
		this.skipMissedRuns = skipMissedRuns;
	}

	/**
	 * Get the last complete date.
	 * 
	 * @return The last complete date, null if never completed.
	 */
	public ScheduleResult getLastComplete() {
	    return lastComplete;
	}

	@Override
	protected IntervalTo getLimits() {
		return null;
	}
	
	@Override
	protected void rescheduleOn(State state) throws ComponentPersistException {
	    State completeOrNot = new CompleteOrNotOp().evaluate(state);
	    if (!(completeOrNot.isComplete()) && haltOnFailure) {
	    	setNextDue(null);
	    }
	    else {
	    	if (completeOrNot.isComplete()) {
				lastComplete = getCurrent();			
	    	}
	    	
	    	Date use = getCurrent().getToDate();
	    	Date now = getClock().getDate();
	    	if (skipMissedRuns && use.before(now)) {
	    		use = now;
	    	}
	    	
	    	scheduleFrom(use);
	    }
	}
	
	protected void reset(Resetable job) {
	    logger().debug("[" + this + "] Sending Hard Reset to [" + job + "]");
	    
    	job.hardReset();
	}
}
