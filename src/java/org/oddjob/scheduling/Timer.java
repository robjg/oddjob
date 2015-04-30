/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.scheduling;

import java.util.Date;

import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.jobs.GrabJob;
import org.oddjob.jobs.job.ResetAction;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.persist.ArchiveJob;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
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
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateOperator;
import org.oddjob.values.SetJob;

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
 * using the <code>from</code> properties instead of the <code>at/in/on</code> 
 * properties of schedules.
 * 
 * <h4>Changing The Next Due Time</h4>
 * 
 * There are two ways to change the next due date of a timer. They both
 * require that the timer has been started but is not yet executing, and they
 * both involve dynamically setting properties of the job which can be done
 * via the 'Job' -&gt; 'Set Property' menu item in Oddjob Explorer or via
 * the {@link SetJob} job within Oddjob.
 * <p>
 * The first method is to set the next due date directly with the 
 * <code>nextDue</code> property. The existing timer is cancelled and the
 * job rescheduled to run at this time. If the time is in the past, the job
 * will run immediately.
 * </p>
 * The second method is to set the the <code>reschedule</code> property with
 * a date and time. The next due date is calculated by applying the date
 * and time the schedule. This is particularly useful for advancing a
 * timer.
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
 * @oddjob.example
 * 
 * Use a timer to stop a long running job.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TimerStopJobExample.xml}
 * 
 * The job will be stopped after 10 seconds. If the job has already completed
 * the stop will have no affect.
 * 
 * @oddjob.example
 * 
 * Manually setting the next due date of the timer. When the set job is 
 * run manually the job will be schedule to run at the new time.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TimerSetNextDueExample.xml}
 * 
 * Note that the <code>current<code> interval property is not changed, so
 * the echo job shows 'Running at 9999-12-31 00:00:00.000'.
 * 
 * @oddjob.example
 * 
 * Manually rescheduling the timer. When the set job is run manually, the
 * timer will advance to it's next scheduled slot.
 * 
 * {@oddjob.xml.resource org/oddjob/scheduling/TimerSetRescheduleExample.xml}
 * 
 * Note that the unlike above, <code>current<code> interval property 
 * changes when the time is rescheduled.
 * 
 * @author Rob Gordon.
 */
public class Timer extends TimerBase {
	
	private static final long serialVersionUID = 2009091420120126L; 
	
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
		
		Date currentTime = getClock().getDate(); 		
		Interval currentInterval = getCurrent(); 
		
		boolean skipMissedRuns = isSkipMissedRuns();
		if (currentInterval != null && 
				(!skipMissedRuns || skipMissedRuns && currentTime.before(
						currentInterval.getToDate()))) {

			logger().info("Setting next due from value of last current property.");
			internalSetNextDue(currentInterval.getFromDate());
		}
		else {
			logger().info("Calculating schedule from current clock date time.");
			scheduleFrom(currentTime);
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


	@Override
	protected IntervalTo getLimits() {
		return null;
	}
	
	@Override
	protected StateCondition getDefaultHaltOn() {
		if (haltOnFailure) {
			return StateConditions.FAILURE;
		}
		else {
			return StateConditions.NONE;
		}
	}
	
	@Override
	protected ResetAction getDefaultReset() {
		return ResetActions.HARD;
	}
}
