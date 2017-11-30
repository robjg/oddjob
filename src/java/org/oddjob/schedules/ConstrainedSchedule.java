package org.oddjob.schedules;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.schedules.schedules.ParentChildSchedule;

/**
 * A base class for a Schedule which has a from and a to
 * date.
 * 
 * @author Rob Gordon
 */
abstract public class ConstrainedSchedule extends AbstractSchedule {

    private static final long serialVersionUID = 20050226;
    
    private static Logger logger = LoggerFactory.getLogger(ConstrainedSchedule.class);

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
	protected final Interval nextInterval(ScheduleContext context) {
	
		Calendar fromCal = fromCalendar(context.getDate(), context.getTimeZone());
		Calendar toCal = toCalendar(context.getDate(), context.getTimeZone());

		Calendar  nowCal = Calendar.getInstance(context.getTimeZone());
		nowCal.setTime(context.getDate());
		
	    if (toCal.before(fromCal)) {
	        if (nowCal.before(toCal)) {
	        	fromCal = shiftFromCalendar(fromCal, -1);
	        }
	    } 
	    else {
	        if (nowCal.compareTo(toCal) >= 0) {
	        	fromCal = shiftFromCalendar(fromCal, 1);
	        }
	    }

        if (nowCal.compareTo(toCal) >= 0) {
        	toCal = shiftToCalendar(toCal, 1);
        }
	    
    	return new SimpleInterval(fromCal.getTime(), toCal.getTime());
	}

	/**
	 * Calculate the last interval.
	 * 
	 * @param context
	 * @return
	 */
	protected final Interval lastInterval(ScheduleContext context) {
		
		Calendar fromCal = fromCalendar(context.getDate(), context.getTimeZone());
		Calendar toCal = toCalendar(context.getDate(), context.getTimeZone());

		Calendar  nowCal = Calendar.getInstance(context.getTimeZone());
		nowCal.setTime(context.getDate());
		
	    if (toCal.before(fromCal)) {
	        if (nowCal.before(toCal)) {
	        	fromCal = shiftFromCalendar(fromCal, -2);
	        }
	        else {
	        	fromCal = shiftFromCalendar(fromCal, -1);
	        }
	    } 
	    else {
	        if (nowCal.before(toCal)) {
	        	fromCal = shiftFromCalendar(fromCal, -1);
	        }
	    }

        if (nowCal.before(toCal)) {
        	toCal = shiftToCalendar(toCal, -1);
        }
	    
        return new SimpleInterval(fromCal.getTime(), toCal.getTime());
	}
	
	/**
	 * Shift the from Calendar by an interval. The subclass fromCalendar
	 * is used to re-adjust the shifted calendar. This is needed
	 * in at least these situations:
	 * <ul>
	 *  <li>A yearly schedule for the month of February that returned
	 *  a toDate in the year before a leap year of the 28th would shift 
	 *  to the 28th as the last day of February in the leap year without 
	 *  re-adjustment.</li>
	 *  <li>A last day of the month schedule that returned 28th in
	 *  February would shift to the 28th of March without re-adjustment.
	 *  </li>
	 * <ul>
	 * 
	 * @param calendar
	 * @param interval.
	 * @return
	 */
	protected Calendar shiftFromCalendar(Calendar calendar, int intervals) {
		calendar = shiftCalendar(calendar, intervals);
		return fromCalendar(calendar.getTime(), calendar.getTimeZone());
	}
	
	/**
	 * Shift the to Calendar by an interval. The subclass toCalendar
	 * is used to re-adjust the shifted calendar for the reasons given
	 * in {@link #shiftFromCalendar(Calendar, int)}
	 * 
	 * @param calendar
	 * @param intervals
	 * @return
	 */
	protected Calendar shiftToCalendar(Calendar calendar, int intervals) {
		calendar = shiftCalendar(calendar, intervals);
		calendar.add(Calendar.MILLISECOND, -1);
		return toCalendar(calendar.getTime(), calendar.getTimeZone());
	}
	
	private Calendar shiftCalendar(Calendar calendar, int intervals) {
		CalendarUnit unit = intervalBetween();
		
    	calendar.add(unit.getField(), intervals * unit.getValue());
		
    	return calendar;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.Schedule#nextDue(java.util.Date)
	 */
	public ScheduleResult nextDue(ScheduleContext context) {
		
		Date now = context.getDate();
		
		if (now == null) {
			return null;
		}
		
		ParentChildSchedule parentChild = 
			new ParentChildSchedule(new Schedule() {
				public ScheduleResult nextDue(ScheduleContext context) {
					return new SimpleScheduleResult(nextInterval(context));
				}
			}, getRefinement());
		
		ScheduleResult nextResult = parentChild.nextDue(context);		
		
		if (nextResult == null) {
			return null;
		}
		
		// If we are before the next interval we need to check we
		// aren't still in the last (because a child interval
		// could extend beyond the limit of it's parent).
		if (now.before(nextResult.getFromDate())) {
			
			parentChild = 
				new ParentChildSchedule(new Schedule() {
					public ScheduleResult nextDue(ScheduleContext context) {
						return new SimpleScheduleResult(lastInterval(context));
					}
				}, getRefinement());
			
			ScheduleResult previous = parentChild.nextDue(context);

			if (previous != null && now.before(previous.getToDate())) {
				nextResult = previous;
			}				
		}
		
		logger.debug(ConstrainedSchedule.this + ": in date is " + now + 
				", next interval is " + nextResult);

		return nextResult;
	}

	
	/**
	 * Force sub classes to override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public abstract String toString();
}
