package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.util.OddjobConfigException;

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
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DateScheduleExamples.xml}
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
	 * along time ago (292 million and something B.C.).
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
	 * @oddjob.description The to date for the schedule. Defaults to the 
	 * a long time away Defaults to (292 million and something A.D.).
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
	 * @param in The on text.
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
	        return new Date(IntervalTo.START_OF_TIME);
	    }
	    
	    TimeZone timeZone = context.getTimeZone();
	    
	    try {
	    	return DateHelper.parseDate(startDate, timeZone);
	    } catch (ParseException e) {
	    	throw new OddjobConfigException("Failed to parse start date ["
	    			+ startDate + "]");
	    }
	    
	}

	/**
	 * Return the end date.
	 * 
	 * @return The end date.
	 */
	Date getEndDate(ScheduleContext context) {
	    if (endDate == null) {
	    	return new Date(IntervalTo.END_OF_TIME);
	    }
	    
	    TimeZone timeZone = context.getTimeZone();
	    
	    try {
	    	return DateUtils.endOfDay(DateHelper.parseDate(endDate, timeZone),
	    			timeZone);
	    } catch (ParseException e) {
	    	throw new OddjobConfigException("Failed to parse end date [" 
	    			+ endDate + "]");
	    }
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
	public IntervalTo nextDue(ScheduleContext context) {
		
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
