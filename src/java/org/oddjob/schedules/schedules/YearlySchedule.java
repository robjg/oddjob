package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.CalendarUtils;
import org.oddjob.schedules.ConstrainedSchedule;
import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.units.Month;

/**
 * @oddjob.description A schedule for the periods of the year. 
 * The periods may be specified either as the dates of days of the year 
 * or as months.
 * <p>
 * The format for dates is MM-dd, i.e. 03-02 is The second of March.
 * <p>
 * The format for months is either the month number, 1 to 12 or one of 
 * JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER,
 * OCTOBER, NOVEMBER, DECEMBER (case insensitive).
 * 
 * @oddjob.description A schedule for a range of months, or a month. The month
 * is specified as an integer between 1 and 12 where 1 is January and 
 * 12 is December.
 * 
 * @oddjob.example
 *
 * A schedule for two different month ranges. From April to September the
 * job will run daily at 10 am, and from October to March the job will run
 * daily at 11 am.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/MonthScheduleExample.xml}
 * 
 * @oddjob.example 
 * 
 * A from day of year to day of year example.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfYearFromTo.xml}
 * 
 * @oddjob.example 
 * 
 * An on day of year example.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfYearOn.xml}
 * 
 * @author Rob Gordon
 */
final public class YearlySchedule extends ConstrainedSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
	public static final String DAY_FORMAT = "MM-dd";

	/**
	 * @oddjob.property
	 * @oddjob.description The from month.
	 * @oddjob.required No, defaults to 1 (January).
	 */
	private Month fromMonth;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The to month.
	 * @oddjob.required No, defaults to 12 (December).
	 */
	private Month toMonth;
				
	@ArooaAttribute
	public void setFromMonth(Month from) {
		this.fromMonth = from;
	}

	public Month getFromMonth() {
	    return fromMonth;
	}
	
	@ArooaAttribute
	public void setToMonth(Month to) {
		this.toMonth = to;
	}
	
	public Month getToMonth() {
	    return toMonth;
	}
	
	/**
     * @oddjob.property inMonth
     * @oddjob.description The month in which this schedule is for. 
     * This has the same effect as setting fromMonth and toMonth to the same thing.
     * @oddjob.required No.
	 * 
	 * @param in The in month.
	 */
	@ArooaAttribute
	public void setInMonth(Month in) {
		this.setFromMonth(in);
		this.setToMonth(in);
	}
			
	/** 
	 * @oddjob.property
	 * @oddjob.description The from month and day.
	 * @oddjob.require No. 
	 */
	private String fromDate;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The to month and day.
	 * @oddjob.require No. 
	 */
	private String toDate;

	
	public void setFromDate(String from) {
		this.fromDate = from;
	}

	public String getFromDate() {
	    return fromDate;
	}

	public void setToDate(String to) {
		this.toDate = to;
	}
	
	public String getToDate() {
	    return toDate;
	}

	/**
     * @oddjob.property onDate
     * @oddjob.description The day on which this schedule is for. 
     * This has the same effect as setting fromDate and toDate to the same thing.
     * @oddjob.required No.
	 * 
	 * @param on The day on which this schedule is for.
	 */
	public void setOnDate(String on) {
		this.setFromDate(on);
		this.setToDate(on);
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
		if (fromDate != null) {
			try {
				return CalendarUtils.dayOfYear(referenceDate,
							parseDay(fromDate, timeZone),
							timeZone);
			}
			catch (ParseException e) {
				throw new RuntimeException("Failed to parse from day.", e);
			}
		}
		else if (fromMonth != null) {
			return CalendarUtils.monthOfYear(referenceDate,
					fromMonth.getMonthNumber(),
					timeZone);
		}
		else {
			return CalendarUtils.startOfYear(referenceDate, timeZone);
		}
	}
	
	protected Calendar toCalendar(Date referenceDate, TimeZone timeZone) {
	    if (toDate != null) {
	    	try {
		    	Calendar cal = CalendarUtils.dayOfYear(referenceDate,
		    				parseDay(toDate, timeZone),
		    				timeZone);
		    	CalendarUtils.setEndOfDay(cal);
			    return cal;
			}
			catch (ParseException e) {
				throw new RuntimeException("Failed to parse to day.", e);
			}
	    }
	    else if (toMonth != null) {
	    	Calendar toCal = CalendarUtils.monthOfYear(referenceDate,
	    			toMonth.getMonthNumber(),
	    			timeZone);
	    	CalendarUtils.setEndOfMonth(toCal);
	    	return toCal;
	    }	    
	    else {
			return CalendarUtils.endOfYear(referenceDate, timeZone);
	    }		
	}
	
	
	/**
	 * Override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public String toString() {
		return this.getClass().getSimpleName() + " from " + getFromDate() + " to " + getToDate();
	}
}
