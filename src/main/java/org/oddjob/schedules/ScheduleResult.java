package org.oddjob.schedules;

import java.util.Date;

import org.oddjob.schedules.schedules.AfterSchedule;

/**
 * The result of calculating the {@link Schedule#nextDue(ScheduleContext)} for a 
 * schedule.
 * 
 * @author rob
 *
 */
public interface ScheduleResult extends Interval {

	/**
	 * For recurring schedules this property provide the date that should be used
	 * for the next call to {@link Schedule#nextDue(ScheduleContext)} by using
	 * the {@link ScheduleContext#move(Date)} method. For most schedules this
	 * date will be identical to the {@link Interval#getToDate()} but for some 
	 * schedules such {@link AfterSchedule} this will not be the case.
	 * 
	 * @return A date. May be null to indicate there is no other next due for 
	 * this schedule..
	 */
	public Date getUseNext();
}
