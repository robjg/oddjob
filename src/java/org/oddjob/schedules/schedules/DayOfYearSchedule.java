package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.CalendarUtils;
import org.oddjob.schedules.ConstrainedSchedule;
import org.oddjob.schedules.DateUtils;

/**
 * @oddjob.description A schedule for the days of the year. 
 * This will most frequently be used to define annual holidays. 
 * <p>
 * The form is MM-dd, i.e. 03-02 is The second of February.
 * 
 * @oddjob.example 
 * 
 * A From To Example.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfYearScheduleFromTo.xml}
 * 
 * @oddjob.example 
 * 
 * An On Example.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfYearScheduleOn.xml}
 * 
 * @author Rob Gordon
 */
final public class DayOfYearSchedule extends ConstrainedSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
	public static final String DAY_FORMAT = "MM-dd";

	/** 
	 * @oddjob.property
	 * @oddjob.description The from month and day.
	 * @oddjob.require No. 
	 */
	private String from;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The to month and day.
	 * @oddjob.require No. 
	 */
	private String to;

	
	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
	    return from;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public String getTo() {
	    return to;
	}

	/**
     * @oddjob.property on
     * @oddjob.description The day on which this schedule is for. 
     * This has the same effect as setting from and to to the same thing.
     * @oddjob.required No.
	 * 
	 * @param on The day on which this schedule is for.
	 */
	public void setOn(String on) {
		this.setFrom(on);
		this.setTo(on);
	}

    @Override
    protected CalendarUnit intervalBetween() {
    	return new CalendarUnit(Calendar.YEAR, 1);
    }
    
	/**
	 * Parse the day of the year.
	 * 
	 * @param text The day of the year
	 * @param timeZone The time zone.
	 * @return
	 */
	static int parseDay(String text, TimeZone timeZone) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat(DAY_FORMAT);
		f.setTimeZone(timeZone);
		Date d = f.parse(text);
		return DateUtils.dayOfYear(d, timeZone);
	}
	
	protected Calendar fromCalendar(Date referenceDate, TimeZone timeZone) {
		if (from == null) {
			return CalendarUtils.startOfYear(referenceDate, timeZone);
		}
		else {
			try {
				return CalendarUtils.dayOfYear(referenceDate,
							parseDay(from, timeZone),
							timeZone);
			}
			catch (ParseException e) {
				throw new RuntimeException("Failed to parse from day.", e);
			}
		}
	}
	
	protected Calendar toCalendar(Date referenceDate, TimeZone timeZone) {
	    if (to == null) {
			return CalendarUtils.endOfYear(referenceDate, timeZone);
	    }
	    else {
	    	try {
		    	Calendar cal = CalendarUtils.dayOfYear(referenceDate,
		    				parseDay(to, timeZone),
		    				timeZone);
		    	CalendarUtils.setEndOfDay(cal);
			    return cal;
			}
			catch (ParseException e) {
				throw new RuntimeException("Failed to parse to day.", e);
			}
	    }		
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
