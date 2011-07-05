package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleContext;

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
	public IntervalTo nextDue(ScheduleContext context) {
		Date now = context.getDate();
		if (now == null) {
			return null;
		}
		
		if (getRefinement() == null) {
		    throw new IllegalStateException("DayAfter must have a child schedule.");
		}

		IntervalTo childInterval = getRefinement().nextDue(context);

		Calendar startCal = new GregorianCalendar();
		startCal.setTime(childInterval.getFromDate());
		startCal.add(Calendar.DATE, 1);
		Date dayAfterStart = startCal.getTime();
		
		Calendar endCal = new GregorianCalendar();
		endCal.setTime(childInterval.getFromDate());
		endCal.add(Calendar.DATE, 2);
		
		return new IntervalTo(dayAfterStart, endCal.getTime());
	}
	
	/**
	 * Override toString.
	 */
	
	public String toString() {
		
		return "Day After Schedule";
	}
}
