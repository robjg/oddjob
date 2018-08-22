package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.OddjobException;
import org.oddjob.arooa.utils.SpringSafeCalendar;
import org.oddjob.arooa.utils.TimeParser;
import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.ConstrainedSchedule;
import org.oddjob.schedules.DateUtils;
import org.oddjob.scheduling.Timer;

/**
 * @oddjob.description A schedule for each day at, or from a given time. 
 * This schedule
 * enables job to be scheduled daily at a particular time or a from/to time which 
 * could be used to constrain a sub schedule.
 * <p>
 * If the 'to' time is less than the 'from' time it is assumed that the 'to'
 * time is the next day. 
 * 
 * @oddjob.example
 * 
 * A simple daily schedule. Used with a {@link Timer} this would run a job
 * every day at 10am. 
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DailyScheduleSimpleExample.xml}
 * 
 * @oddjob.example
 * 
 * Using an interval with a daily schedule to schedules something every 15 minutes 
 * between 10pm and 4am. The end time is 03:50 yet the last interval is
 * 03:45 to 04:00 because the interval starts before the end time.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DailyWithIntervalExample.xml}
 * 
 * @author Rob Gordon
 */

final public class DailySchedule extends ConstrainedSchedule implements Serializable {
    private static final long serialVersionUID = 20050226;
    	
	private String from;
	private String to;
	
    /**
     * @oddjob.property from
     * @oddjob.description The from time.
     * @oddjob.required No. Default to the start of the day.
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
     * @oddjob.description The to time.
     * @oddjob.required No. Default to the end of the day.
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
	
    @Override
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

	protected Calendar fromCalendar(Date referenceDate, TimeZone timeZone) {
		Calendar fromCal = Calendar.getInstance(timeZone);
		if (from == null) {
    		fromCal.setTime(DateUtils.startOfDay(referenceDate, timeZone));
		}
		else {
			fromCal.setTime(parseTime(from, referenceDate, timeZone, "from"));
		}
		return fromCal;
	}
	
	protected Calendar toCalendar(Date referenceDate, TimeZone timeZone) {
		Calendar toCal = Calendar.getInstance(timeZone);
	    if (to == null) {
	    	toCal.setTime(DateUtils.endOfDay(referenceDate, timeZone));
	    }
	    else {
	    	toCal.setTime(parseTime(to, referenceDate, timeZone, "to"));
	    }
	    
	    Calendar fromCal = fromCalendar(referenceDate, timeZone);
    	if (toCal.equals(fromCal)) {
    		// For 'at' times.
    		toCal.add(Calendar.MILLISECOND, 1);
    	}
	    
		return toCal;
	}
	
	/**
	 * Override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public String toString() {

		String from;
		
		if (this.from == null) {
			from = "the start of the day";
		} else {
			from = this.from;
		}
		
		String to;
		
		if (this.to == null) {
			to = "the end of the day";
		}
		else {
			to = this.to;
		}
		
		StringBuilder description = new StringBuilder();
		if (from.equals(to)) {
			description.append(" at ");
			description.append(from);
		}
		else {
			description.append(" from ");
			description.append(from);
			description.append(" to ");
			description.append(to);
		}
		
		if (getRefinement() != null) {
			description.append(" with refinement ");
			description.append(getRefinement().toString());
		}
		
		return "Daily" + description;
	}
}
