package org.oddjob.schedules;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.oddjob.schedules.schedules.ParentChildSchedule;

/**
 * A base class for a Schedule which has a from and a to
 * date.
 * 
 * @author Rob Gordon
 */

abstract public class ConstrainedSchedule extends AbstractSchedule {

    private static final long serialVersionUID = 20050226;
    
    private static Logger logger = Logger.getLogger(ConstrainedSchedule.class);

    /**
     * Provide a Calendar for the start of the constraint.
     * 
     * @param referenceDate The date/time now.
     * @param timeZone The time zone.
     * 
     * @return A calendar for the from time.
     */
	abstract protected Calendar fromCalendar(Date referenceDate, TimeZone timeZone);
	
    /**
     * Provide a Calendar for the end of the constraint.
     * 
     * @param referenceDate The date/time now.
     * @param timeZone The time zone.
     * 
     * @return A calendar for the end time.
     */
	abstract protected Calendar toCalendar(Date referenceDate, TimeZone timeZone);
	
	
	/**
	 * Sub classes must provide a unit which is what must be
	 * added to move the schedule on. I.e. the equivalent of a day, week,
	 * month etc.
	 * 
	 * @return
	 */
	protected abstract CalendarUnit intervalBetween();
	
	/**
	 * Calculate the next interval, without children.
	 * 
	 * @param context
	 * @return
	 */
	protected final IntervalTo nextInterval(ScheduleContext context) {
	
		Calendar fromCal = fromCalendar(context.getDate(), context.getTimeZone());
		Calendar toCal = toCalendar(context.getDate(), context.getTimeZone());

		CalendarUnit unit = intervalBetween();
		
		Calendar  nowCal = Calendar.getInstance(context.getTimeZone());
		nowCal.setTime(context.getDate());
		
	    if (fromCal.after(toCal)) {
	        if (!nowCal.after(toCal)) {
	        	fromCal.add(unit.getField(), -1 * unit.getValue());
	        }
	    } 
	    else {
	        if (nowCal.after(toCal)) {
	        	fromCal.add(unit.getField(), unit.getValue());
	        }
	    }

        if (nowCal.after(toCal)) {
        	// Need to re-get from time because of leap years.
        	// - The end of February might change.
        	toCal.add(unit.getField(), unit.getValue());
        	ScheduleContext shiftedContext = context.move(toCal.getTime());
        	toCal = toCalendar(shiftedContext.getDate(), context.getTimeZone());
        }
	    
	    return new IntervalTo(
	    		new Interval(fromCal.getTime(), toCal.getTime()));
	}

	/**
	 * Calculate the last interval.
	 * 
	 * @param context
	 * @return
	 */
	protected final IntervalTo lastInterval(ScheduleContext context) {
		
		Calendar fromCal = fromCalendar(context.getDate(), context.getTimeZone());
		Calendar toCal = toCalendar(context.getDate(), context.getTimeZone());

		CalendarUnit unit = intervalBetween();
		
		Calendar  nowCal = Calendar.getInstance(context.getTimeZone());
		nowCal.setTime(context.getDate());
		
	    if (fromCal.after(toCal)) {
	        if (nowCal.before(toCal)) {
	        	fromCal.add(unit.getField(), -2 * unit.getValue());
	        }
	        else {
	        	fromCal.add(unit.getField(), -1 * unit.getValue());	        	
	        }
	    } 
	    else {
	        if (!nowCal.after(toCal)) {
	        	fromCal.add(unit.getField(), -1 * unit.getValue());
	        }
	    }

        if (nowCal.before(toCal)) {
        	// Need to re-get from time because of leap years.
        	// - The end of February might change.
        	toCal.add(unit.getField(), -1 * unit.getValue());
        	ScheduleContext shiftedContext = context.move(toCal.getTime());
        	toCal = toCalendar(shiftedContext.getDate(), context.getTimeZone());
        }
	    
	    return new IntervalTo(
	    		new Interval(fromCal.getTime(), toCal.getTime()));
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.Schedule#nextDue(java.util.Date)
	 */
	public IntervalTo nextDue(ScheduleContext context) {
		
		Date now = context.getDate();
		
		if (now == null) {
			return null;
		}
		
		ParentChildSchedule parentChild = 
			new ParentChildSchedule(new Schedule() {
				public IntervalTo nextDue(ScheduleContext context) {
					return nextInterval(context);
				}
			}, getRefinement());
		
		IntervalTo nextInterval = parentChild.nextDue(context);		
		
		if (nextInterval == null) {
			return null;
		}
		
		if (now.before(nextInterval.getFromDate())) {
			
			parentChild = 
				new ParentChildSchedule(new Schedule() {
					public IntervalTo nextDue(ScheduleContext context) {
						return lastInterval(context);
					}
				}, getRefinement());
			
			IntervalTo previous = parentChild.nextDue(context);

			if (previous != null && !now.after(previous.getToDate())) {
				return nextInterval = previous;
			}				
		}
		
		logger.debug(ConstrainedSchedule.this + ": in date is " + now + 
				", next interval is " + nextInterval);

		return nextInterval;
	}

	/**
	 * Force sub classes to override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public abstract String toString();
}
