package org.oddjob.schedules;




/**
 * The interface that defines a schedule.
 * 
 * @author Rob Gordon
 */

public interface Schedule {

	/**
	 * For a given date a schedule will provide the interval this schedule
	 * should next be scheduled in.
	 * <p>
	 * If the schedule is never due again for the given date,
	 * null is returned.
	 * <p>
	 * @param now The date now.
	 * @return The next due interval for the schedule.
	 */
	public ScheduleResult nextDue(ScheduleContext context);

}
