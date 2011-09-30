/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;

import org.oddjob.Resetable;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.jobs.GrabJob;
import org.oddjob.persist.ArchiveJob;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.schedules.BrokenSchedule;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.schedules.schedules.DateSchedule;
import org.oddjob.schedules.schedules.DayAfterSchedule;
import org.oddjob.schedules.schedules.DayBeforeSchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.schedules.schedules.MonthlySchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.schedules.schedules.YearlySchedule;
import org.oddjob.state.CompleteOrNotOp;
import org.oddjob.state.State;
import org.oddjob.state.StateOperator;

/**
 * @oddjob.description Provides a simple timer for periodic or once only 
 * execution of the child job.
 * <p>
 * 
 * <h4>Schedules</h4>
 * 
 * Once only execution:
 * <ul>
 *  <li>{@link TimeSchedule}</li>
 *  <li>{@link DateSchedule}</li>
 *  <li>{@link CountSchedule} (With a count of 1)</li>
 * </ul> 
 * Recurring executions:
 * <ul>
 *  <li>{@link YearlySchedule}</li>
 *  <li>{@link MonthlySchedule}</li>
 *  <li>{@link WeeklySchedule}</li>
 *  <li>{@link DailySchedule}</li>
 *  <li>{@link IntervalSchedule}</li>
 * </ul> 
 * Holidays:
 * <ul>
 *  <li>{@link BrokenSchedule}</li>
 *  <li>{@link DayAfterSchedule}</li>
 *  <li>{@link DayBeforeSchedule}</li>
 * </ul> 
 *  
 * <h4>Missed Executions</h4>
 * <p>
 * If Oddjob is running with a persister missed executions fire immediately one
 * after the other until all missed executions have run.
 * <p>
 * This can be overridden with the <code>skipMissedRuns</code> property.
 * <p>
 * If a timer is started after the initial execution time but within the interval
 * of the schedule - execution will happen immediately. Extended intervals are created
 * using the <i>from</i> properties instead of the <i>at/in/on</i> properties
 * of schedules.
 * 
 * <h4>Retrying Failed Jobs</h4>
 * 
 * Nest a {@link Retry} job.
 * 
 * <h4>Recording the Outcome of Runs</h4>
 * 
 * Nest an {@link ArchiveJob}.
 * 
 * <h4>Distributed Scheduling</h4>
 * 
 * Nest a {@link GrabJob}.
 * 
 * <h4>For More Information</h4>
 *
 * For more information see the Scheduling section of the User Guide.
 * 
 * 
 * @oddjob.example
 * 
 * A Timer that runs at 10am each day, Monday to Friday.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TimerExample.xml}
 * 
 * @oddjob.example
 * 
 * Run once at 10am or any time after.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TimerOnceExample.xml}
 * 
 * If the report completes before 10am the timer will schedule it to be e-mailed
 * at 10am. If the report completes after 10am it is e-mailed immediately.
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
	protected void begin() throws ComponentPersistException {

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
	    	
	    	Date use = getCurrent().getUseNext();
	    	Date now = getClock().getDate();
	    	if (use != null &&  skipMissedRuns && use.before(now)) {
	    		use = now;
	    	}
	    	
	    	scheduleFrom(use);
	    }
	}
	
	
	protected void onReset() {
		super.onReset();
		
		lastComplete = null;
	}
	
	protected void reset(Resetable job) {
	    logger().debug("[" + this + "] Sending Hard Reset to [" + job + "]");
	    
    	job.hardReset();
	}
}
