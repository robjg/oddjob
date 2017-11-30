package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.OddjobException;
import org.oddjob.arooa.utils.SpringSafeCalendar;
import org.oddjob.arooa.utils.TimeParser;
import org.oddjob.schedules.AbstractSchedule;
import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.ConstrainedSchedule;
import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;
import org.oddjob.scheduling.Timer;

/**
 * @oddjob.description Provide a schedule for an interval of time. When used as a
 * refinement this schedule will narrow the parent interval down to an interval of 
 * time on the first day of the parent interval, or if the <code>toLast</code>
 * property is specified, from the first day to the last day of the parent interval. When used as the 
 * topmost definition for a schedule then this schedule specifies a single interval
 * of time starting on the current day.
 * <p>
 * To provide a schedule for each day at a certain time see the {@link DailySchedule}
 * schedules.
 * 
 * @oddjob.example
 * 
 * A simple time example.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/TimeScheduleSimpleExample.xml}
 * 
 * When used with a {@link Timer} this would run a job just once at 10am, and
 * never again. If the
 * timer was started after 10am, then the job would run the following day at 10am.
 * If it was required that the job would run any time the timer was started 
 * after 10am then the <code>
 * from</code> property should be used instead of the <code>at</code> property.
 * 
 * @oddjob.example
 * 
 * Using an interval with time to schedule something every 15 minutes between 
 * 10pm and 4am the next day. The end time is 03:50 yet the last interval is
 * 03:45 to 04:00 because the interval starts before the end time.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/TimeAndIntervalExample.xml}
 * 
 * @oddjob.example
 * 
 * Schedule something over a whole week between two times. This demonstrates
 * how the <code>toLast</code> property works.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/TimeOverWeekExample.xml}
 * 
 * The schedule would be due every two hours all day and all night from 8am 
 * Monday morning until 6pm Friday afternoon.
 * 
 * @author Rob Gordon
 */

final public class TimeSchedule extends AbstractSchedule implements Serializable {

	private static Logger logger = LoggerFactory.getLogger(ConstrainedSchedule.class);
	
    private static final long serialVersionUID = 200502262011092000L;
    	
	private String from;
	
	private String to;
	
	private String toLast;
	
    /**
     * @oddjob.property from
     * @oddjob.description The from time.
     * @oddjob.required No. Defaults to the start of any parent interval
     * or the beginning of time.
     * 
     * @param from The from date.
     */
	public void setFrom(String from) {
		this.from = from; 
	}

	/*
	 *  (non-Javadoc)
	 * @see org.treesched.ConstrainedSchedule#getFrom()
	 */
	public String getFrom() {
	    return from;
	}
		
    /**
     * @oddjob.property to
     * @oddjob.description The to time. If specified, this is the
     * time on the first day of the parent interval.
     * @oddjob.required No. Defaults to the end of the last day of the
     * parent interval, or the end of time.
     * 
     * @param to The to date.
     * 
     */
	public void setTo(String to) {
		this.to = to;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.ConstrainedSchedule#getTo()
	 */
	public String getTo() {
	    return to;
	}

	/**
     * @oddjob.property at
     * @oddjob.description The time at which this schedule is for. 
     * This has the same effect as setting from and to to the same thing.
     * @oddjob.required No.
	 * 
	 * @param at The at time.
	 */
	public void setAt(String at) {
		this.setFrom(at);
		this.setTo(at);
	}
	
    public String getToLast() {
		return toLast;
	}

    /**
     * @oddjob.property toLast
     * @oddjob.description The to time for the end of the parent interval.
     * This differs from the to property in that the to property is for the first
     * day of the parent interval.
     * @oddjob.required No. The to property, or it's default value,
     * will be used instead.
     * 
     * @param toLast The to last time of the interval.
     */
	public void setToLast(String toLast) {
		this.toLast = toLast;
	}

	protected CalendarUnit intervalBetween() {
    	return new CalendarUnit(Calendar.DATE, 1);
    }

	static Date parseTime(String textField, Date referenceDate, 
			TimeZone timeZone, String fieldName) {
		TimeParser timeFormatter = new TimeParser(
				new SpringSafeCalendar(referenceDate, timeZone));		
		try {
			Date now = timeFormatter.parse(textField);
			return now;
		} catch (ParseException e) {
			throw new OddjobException("Failed to parse " + fieldName
					+ "[" + textField + "]");
		}
	}	

	protected Calendar fromCalendar(ScheduleContext context) {
		
		TimeZone timeZone = context.getTimeZone();
		
		Calendar fromCal = Calendar.getInstance(timeZone);
		
		Interval parentInterval = context.getParentInterval();
		
		if (from == null) {
			if (parentInterval == null) {
				fromCal.setTime(Interval.START_OF_TIME);
			}
			else {
				fromCal.setTime(DateUtils.startOfDay(parentInterval.getFromDate(), timeZone));
			}
		}
		else {
			if (parentInterval == null) {
				fromCal.setTime(parseTime(from, context.getDate(), timeZone, "from"));
			}
			else {
				fromCal.setTime(parseTime(from, parentInterval.getFromDate(), timeZone, "from"));
			}
		}
		
		return fromCal;
	}
	
	protected Calendar toCalendar(ScheduleContext context) {
		
		TimeZone timeZone = context.getTimeZone();
		
		Calendar toCal = Calendar.getInstance(timeZone);
		
		Interval parentInterval = context.getParentInterval();
		
		if (toLast != null) {
	    	if (parentInterval == null) {
		    	toCal.setTime(parseTime(to, context.getDate(), timeZone, "to"));
	    	}
	    	else {
		    	toCal.setTime(parseTime(to, DateUtils.oneMillisBefore(parentInterval.getToDate()), timeZone, "to"));	    		
	    	}
		}
	    if (to != null) {
	    	if (parentInterval == null) {
		    	toCal.setTime(parseTime(to, context.getDate(), timeZone, "to"));
	    	}
	    	else {
		    	toCal.setTime(parseTime(to, parentInterval.getFromDate(), timeZone, "to"));	    		
	    	}
	    }
	    else {
	    	if (parentInterval == null) {
	    		toCal.setTime(Interval.END_OF_TIME);
	    	}
	    	else {
	    		toCal.setTime(DateUtils.endOfDay(
	    				DateUtils.oneMillisBefore(parentInterval.getToDate()), timeZone));
	    	};
	    }
	    
		return toCal;
	}
	
	/**
	 * @param context
	 * @return
	 */
	protected Calendar nowCalendar(ScheduleContext context) {
		
		Calendar  nowCal = Calendar.getInstance(context.getTimeZone());
		nowCal.setTime(context.getDate());
		
		Interval parentInterval = context.getParentInterval();
		if (parentInterval != null) {
			
			if (parentInterval.getToDate().compareTo(context.getDate()) <= 0) {
				nowCal.setTime(DateUtils.oneMillisBefore(parentInterval.getToDate()));
			}
			else if (parentInterval.getFromDate().compareTo(context.getDate()) > 0) {
				nowCal.setTime(parentInterval.getFromDate());
			}
		}
		
		return nowCal;
	}
	
	/**
	/**
	 * Calculate the next interval, without children.
	 * 
	 * @param context
	 * @return
	 */
	protected final Interval nextInterval(ScheduleContext context) {
	
		Calendar fromCal = fromCalendar(context);
		Calendar toCal = toCalendar(context);
				
		if (fromCal.getTime().equals(toCal.getTime())) {
			toCal.add(Calendar.MILLISECOND, 1);
		}
		
		Calendar  nowCal = nowCalendar(context);
		
	    if (fromCal.after(toCal)) {
	        toCal = shiftFromCalendar(toCal, 1);
	    }

	    if (nowCal.compareTo(toCal) >= 0) {
	    	return null;
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
		
		Calendar fromCal = fromCalendar(context);
		Calendar toCal = toCalendar(context);
				
		if (fromCal.getTime().equals(toCal.getTime())) {
			toCal.add(Calendar.MILLISECOND, 1);
		}

		Calendar  nowCal = nowCalendar(context);
		
	    if (fromCal.after(toCal)) {
	        fromCal = shiftFromCalendar(fromCal, -1);	        
	    }

	    if (nowCal.compareTo(toCal) < 0) {
	    	return null;
	    }	    
        
	    return new SimpleInterval(fromCal.getTime(), toCal.getTime());		
	}

	
	protected Calendar shiftFromCalendar(Calendar calendar, int intervals) {
		if (calendar.getTime().equals(Interval.START_OF_TIME)) {
			return calendar;
		}
		else {
			return shiftCalendar(calendar, intervals);
		}
	}
	
	protected Calendar shiftToCalendar(Calendar calendar, int intervals) {
		if (calendar.getTime().equals(Interval.END_OF_TIME)) {
			return calendar;
		}
		else {
			return shiftCalendar(calendar, intervals);
		}
	}
	
	private Calendar shiftCalendar(Calendar calendar, int intervals) {
		CalendarUnit unit = intervalBetween();
		
    	calendar.add(unit.getField(), intervals * unit.getValue());
		
    	return calendar;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.schedules.Schedule#nextDue(org.oddjob.schedules.ScheduleContext)
	 */
	public ScheduleResult nextDue(ScheduleContext context) {
		
		Date now = context.getDate();
		
		if (now == null) {
			return null;
		}
		
		final Interval thisNextInterval = nextInterval(context);
		
		Interval thisInterval = thisNextInterval;
		ScheduleResult nextResult = null;
		
		if (thisNextInterval != null) {
		
			ParentChildSchedule parentChild = 
				new ParentChildSchedule(new Schedule() {
					public ScheduleResult nextDue(ScheduleContext context) {
						return new SimpleScheduleResult(thisNextInterval);
					}
				}, getRefinement());
			
			nextResult = parentChild.nextDue(context);
		}
		
		
		// Maybe we are beyond the interval but still in the interval of 
		// a child (because a child interval could extend beyond the limit 
		// of it's parent).
		if ((nextResult == null || now.before(nextResult.getFromDate())) &&
				// We need this extra check because time intervals can extend forever.
				(to != null && getRefinement() != null)) {

			final Interval thisPreviousInterval = lastInterval(context);

			if (thisPreviousInterval != null) {
				ParentChildSchedule parentChild = 
					new ParentChildSchedule(new Schedule() {
						public ScheduleResult nextDue(ScheduleContext context) {
							return new SimpleScheduleResult(thisPreviousInterval);
						}
					}, getRefinement());
	
				ScheduleResult previous = parentChild.nextDue(context);
	
				if (previous != null && now.before(previous.getToDate())) {
					nextResult = previous;
					thisInterval = thisPreviousInterval;
				}
			}
		}
				
		if (nextResult == null) {
			return null;
		}
		
		if (!thisInterval.getToDate().after(nextResult.getToDate())) {
			// time is a once only schedule.
		 	nextResult = new SimpleScheduleResult(nextResult, null);		
		}
	 	
	 	logger.debug(this + ": in date is " + now + 
				", next interval is " + nextResult);

		return nextResult;
	}
	
	/**
	 * Override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public String toString() {

		String from;
		
		if (this.from == null) {
			from = null;
		} else {
			from = this.from;
		}
		
		String to;
		
		if (this.toLast != null) {
			to = "last " + this.toLast;
		}
		if (this.to != null) {
			to = this.to;
		}
		else {
			to = null;
		}
		
		StringBuilder description = new StringBuilder();
		if (from != null && from.equals(to)) {
			description.append(" at ");
			description.append(from);
		}
		else {
			if (from != null) {
				description.append(" from ");
				description.append(from);
			}
			if (to != null) {
				description.append(" to ");
				description.append(to);
			}
		}
		
		if (getRefinement() != null) {
			description.append(" with refinement ");
			description.append(getRefinement().toString());
		}
		
		return "Time" + description;
	}
}
