package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.CalendarUtils;
import org.oddjob.schedules.ConstrainedSchedule;

/**
 * @oddjob.description Implement a schedule based on days of the month. The day of the month is given
 * as an integer, but unlike the java GregorianCalander, 0 and negative numbers are taken to be
 * this month, not the previous month. i.e. on="0" is the last day of the month.
 * <p>
 * 
 * @oddjob.example
 * 
 * A range of days of the month.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfMonthExample1.xml}
 * 
 * This would schedule a job to run every day from the 17th of each month to
 * the 25th of each mont at 10am.
 * 
 * @oddjob.example
 * 
 * On a single day of the month.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfMonthExample2.xml}
 * 
 * This will run a job on the 15ht of every month.
 * 
 * @author Rob Gordon
 */


final public class DayOfMonthSchedule extends ConstrainedSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
    /** from day of month. */
    private Integer fromDay;
    
    /** to day of month. */
    private Integer toDay;
        
    /**
     * @oddjob.property from
     * @oddjob.description The from day of the month.
     * @oddjob.required No. Defaults to 1.
     * 
     * @param from The from date.
     */
    public void setFrom(String from) {
        if (from == null) {
            fromDay = null;
        }
        else {
            fromDay = new Integer(from);            
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.treesched.ConstrainedSchedule#getFrom()
     */
    public String getFrom() {
        if (fromDay == null) {
            return null;
        }
        return fromDay.toString();
    }
    
    /**
     * @oddjob.property to
     * @oddjob.description The to day of the month.
     * @oddjob.required No. Defaults to the last day of the month.
     * 
     * @param to The to date.
     */
    public void setTo(String to) {
        if (to == null) {
            toDay = null;
        }
        else {
            toDay = new Integer(to);
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.treesched.ConstrainedSchedule#getTo()
     */
    public String getTo() {
        if (toDay == null) {
            return null;
        }
        return toDay.toString();
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
    	return new CalendarUnit(Calendar.MONTH, 1);
    }

	/**
	 * Get a calendar for the from day.
	 * 
	 * @param referenceDate The date to get month, year info from.
	 * @param timeZone The time zone.
	 * 
	 * @return A calendar.
	 */
	protected Calendar fromCalendar(Date referenceDate, TimeZone timeZone) {
		if (fromDay == null) {
	    	return CalendarUtils.startOfMonth(referenceDate, timeZone);
	    }
		else {		
			return CalendarUtils.dayOfMonth(referenceDate, fromDay.intValue(), timeZone);
		}
	}
	
	/**
	 * Get a calendar for the to day.
	 * 
	 * @param referenceDate The date to get month, yar info from.
	 * @param timeZone The time zone.
	 * 
	 * @return A calendar. 
	 */
	protected Calendar toCalendar(Date referenceDate, TimeZone timeZone) {
	    if (toDay == null) {
	    	return CalendarUtils.endOfMonth(referenceDate, timeZone);
	    }
	    else {		
	    	Calendar cal = CalendarUtils.dayOfMonth(referenceDate, toDay.intValue(), timeZone);
	    	CalendarUtils.setEndOfDay(cal);
	    	return cal;
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
