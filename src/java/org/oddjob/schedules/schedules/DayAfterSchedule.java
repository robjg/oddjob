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

/**
 * @oddjob.description A schedule that returns the day after when a 
 * nested schedule is due.
 * <p>
 * This is designed to be used for processing which happens the
 * day after, i.e. processing previous business days data.
 * <p>
 * This is particularly useful for scheduling around holidays when a process
 * is still required to run on the holiday, but not the day after the holiday.
 * 
 * @oddjob.example
 * 
 * An example to run at 2am Tuesday to Saturday.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayAfterScheduleExample.xml}
 * 
 * @author Rob Gordon
 */

final public class DayAfterSchedule extends AbstractSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
    /*
     *  (non-Javadoc)
     * @see org.treesched.Schedule#nextDue(java.util.Date)
     */
	public ScheduleResult nextDue(ScheduleContext context) {
		Date use = null;
		
		Interval interval = context.getParentInterval();
		if (interval != null) {
			use = new Date(interval.getToDate().getTime() - 1L);
		}
		else {
			use = context.getDate();
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
				useCal.get(Calendar.DATE) + 1);
				
		Calendar endCal = new GregorianCalendar();
		endCal.clear();
		endCal.set(
				useCal.get(Calendar.YEAR), 
				useCal.get(Calendar.MONTH), 
				useCal.get(Calendar.DATE) + 2);
		
		IntervalTo newInterval = new IntervalTo(
				startCal.getTime(), endCal.getTime());
		
		if (getRefinement() != null) {
			
			ScheduleContext shiftedContext = context.spawn(
					startCal.getTime(), 
					newInterval);
			
			return getRefinement().nextDue(shiftedContext);
		}
		else {
			return newInterval;
		}
	}
	
	/**
	 * Override toString.
	 */
	
	public String toString() {
		
		String description = "";
		
		if (getRefinement() != null) {
			description = " with refinement " + getRefinement();
		}
		return "Day After" + description;
	}
}
