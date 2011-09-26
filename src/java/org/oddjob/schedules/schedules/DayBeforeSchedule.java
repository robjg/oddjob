package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * @oddjob.description A schedule that returns the day before when it's
 * parent schedule is due.
 * <p>
 * This is designed to be used with the {@link BrokenSchedule}'s alternative
 * property to move processing to the day before the holiday.
 * <p>
 * 
 * @oddjob.example
 * 
 * A schedule for the last day of the month, or the previous working day
 * if the last day falls on a non working day.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayBeforeScheduleExample.xml}
 * 
 * Note that the refinement schedules for a different time when the day before is 
 * used. This reflects the situation where data is often available later before
 * a weekend or holiday.
 * 
 * @author Rob Gordon
 */
public class DayBeforeSchedule extends AbstractSchedule implements Serializable {

    private static final long serialVersionUID = 2011092200L;
    
    /*
     * (non-Javadoc)
     * @see org.oddjob.schedules.Schedule#nextDue(org.oddjob.schedules.ScheduleContext)
     */
	public ScheduleResult nextDue(ScheduleContext context) {
		Date use = null;
		Date useNext = null;
		
		Interval interval = context.getParentInterval();
		if (interval != null) {
			use = interval.getFromDate();
			useNext = interval.getToDate();
		}
		else {
			use = context.getDate();
			useNext = use;
		}
		if (use == null) {
			return null;
		}
			
						
		Calendar useCal = Calendar.getInstance(context.getTimeZone());
		useCal.setTime(use);
		
		Calendar startCal = Calendar.getInstance(context.getTimeZone());
		startCal.clear();
		startCal.set(
				useCal.get(Calendar.YEAR), 
				useCal.get(Calendar.MONTH), 
				useCal.get(Calendar.DATE) - 1);
				
		Calendar endCal = new GregorianCalendar();
		endCal.clear();
		endCal.set(
				useCal.get(Calendar.YEAR), 
				useCal.get(Calendar.MONTH), 
				useCal.get(Calendar.DATE));
		
		Interval newInterval = new IntervalTo(
				startCal.getTime(), endCal.getTime());
		
		Interval result;
		
		if (getRefinement() != null) {
			
			ScheduleContext shiftedContext = context.spawn(
					startCal.getTime(), 
					newInterval);
			
			result = getRefinement().nextDue(shiftedContext);
		}
		else {
			result = newInterval;
		}
		
		if (result == null) {
			return null;
		}
		
		return new SimpleScheduleResult(result, useNext);
	}
	
	/**
	 * Override toString.
	 */
	public String toString() {
		
		String description = "";
		
		if (getRefinement() != null) {
			description = " with refinement " + getRefinement();
		}

		return "Day Before" + description;
	}
}
