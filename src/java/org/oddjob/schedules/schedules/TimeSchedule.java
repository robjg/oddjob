package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.OddjobException;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.ConstrainedSchedule;
import org.oddjob.schedules.DateUtils;

/**
 * @oddjob.description A schedule for the times of the day. This schedule
 * enables job to be schedule at a paticular time or a from/to time which 
 * could be used to constrain a sub schedule.
 * <p>
 * Please note the property 'on' is used instead of at, this is due to a
 * lazy developer sharing logic with other constrained schedules such as
 * {@link DayOfYearSchedule}.
 * <p>
 * If the 'to' time is less than the 'from' time it is assumed that the 'to'
 * time is the next day. 
 * 
 * 
 * @oddjob.example
 * 
 * A simple time example.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/TimeScheduleSimpleExample.xml}
 * 
 * @oddjob.example
 * 
 * Using an interval with time to schedule something every 15 minutes between 
 * 10pm and 4am the next day. The end time is 03:50 yet the last interval is
 * 03:45 to 04:00 because the interval starts before the end time.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/TimeAndIntervalExample.xml}
 * 
 * @author Rob Gordon
 */

final public class TimeSchedule extends ConstrainedSchedule implements Serializable {
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
		String dateText = DateHelper.formatDate(referenceDate);
		try {
			return DateHelper.parseDateTime(dateText + " " + textField, timeZone);
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
	    	if (to.equals(from)) {
	    		toCal.add(Calendar.MILLISECOND, 1);
	    	}
	    }
	    
		return toCal;
	}
	
	/**
	 * Override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public String toString() {
		return this.getClass().getSimpleName() + " from " + getFrom() + " to " + getTo();
	}
}
