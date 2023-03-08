package org.oddjob.schedules.schedules;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.*;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * @oddjob.description Provide a schedule for a 
 * specific date or define an interval between two dates.
 * <p>
 * The dates must be of the form yyyy-MM-dd 
 * where the format is as specified by the Java Date Format.
 * 
 * @oddjob.example
 * 
 * A schedule for Christmas.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DateScheduleExample.xml}
 * 
 * @author Rob Gordon
 */
final public class DateSchedule extends AbstractSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
	
	private String startDate;
	private String endDate;
	
	/**
	 * @oddjob.property from
	 * @oddjob.description The from date for the schedule. Defaults to 
	 * along time ago.
	 * @oddjob.required No.
	 * 
	 * @param startDateString The from date. May be null.
	 * @throws ParseException If the String isn't a Date.
	 */
	public void setFrom(String startDateString) {
		startDate = startDateString;
	}
	
	/**
	 * Get the from Date as a String.
	 * 
	 * @return The from date. May be null.
	 */
	public String getFrom() {
		return startDate;
	}

	/**
	 * @oddjob.property to
	 * @oddjob.description The to date for the schedule. This date is
	 * inclusive, the defined interval is up to and including the last
	 * millisecond of this date. This defaults to
	 * a long time away.
	 * @oddjob.required No.
	 * 
	 * @param endDateString The end date. May be null.
	 * @throws ParseException If the string isn't a valid date.
	 */
	public void setTo(String endDateString) {
		endDate = endDateString;
	}

	/**
	 * Return the to date as a string.
	 * 
	 * @return The to date, may be null.
	 */
	public String getTo() {
		return endDate;
	}

	/**
	 * @oddjob.property on
	 * @oddjob.description A specific date on which to schedule something.
	 * @oddjob.required No.
	 * 
	 * @param on The on text.
	 * @throws ParseException If the string isn't a date.
	 */
	public void setOn(String on) {		
		setFrom(on);
		setTo(on);
	}

	
	/**
	 * Return the start date.
	 * 
	 * @return The start date.
	 */	
	Date getStartDate(ScheduleContext context) {
	    if (startDate == null) {
	        return Interval.START_OF_TIME;
	    }
	    
	    TimeZone timeZone = context.getTimeZone();
	    
		return DateHelper.parseDate(startDate, timeZone);

	}

	/**
	 * Return the end date.
	 * 
	 * @return The end date.
	 */
	Date getEndDate(ScheduleContext context) {
	    if (endDate == null) {
	    	return Interval.END_OF_TIME;
	    }
	    
	    TimeZone timeZone = context.getTimeZone();
	    
		return DateUtils.endOfDay(DateHelper.parseDate(endDate, timeZone),
	    			timeZone);
	}

	class ThisSchedule implements Schedule {
		
		public IntervalTo nextDue(ScheduleContext context) {

			Date now = context.getDate();
			if (now == null) {
				return null;
			}
		    if (now.compareTo(getEndDate(context)) >= 0) {
		        return null;
		    }
		    
		    return new IntervalTo(new IntervalTo(
		    		getStartDate(context), 
					getEndDate(context)));
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.Schedule#nextDue(java.util.Date)
	 */
	public ScheduleResult nextDue(ScheduleContext context) {
		
		ParentChildSchedule parentChild = 
			new ParentChildSchedule(new ThisSchedule(), getRefinement());
		
		return parentChild.nextDue(context);		
	}
	
	/**
	 * Override toString.
	 */
	public String toString() {
		
		return this.getClass().getName() + " from " + getFrom() + " to " + getTo();
	}
}
