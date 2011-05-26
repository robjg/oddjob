package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.CalendarUtils;
import org.oddjob.schedules.ConstrainedSchedule;

/**
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
 *  
 * @author Rob Gordon
 */

final public class MonthSchedule extends ConstrainedSchedule 
implements Serializable {

    private static final long serialVersionUID = 20050226;
    
	/**
	 * @oddjob.property
	 * @oddjob.description The from month.
	 * @oddjob.required No, defaults to 1 (January).
	 */
	private Integer from;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The to month.
	 * @oddjob.required No, defaults to 12 (December).
	 */
	private Integer to;
				
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.ConstrainedSchedule#setFrom(java.lang.String)
	 */
	public void setFrom(Integer from) {
		this.from = from;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.treesched.ConstrainedSchedule#getFrom()
	 */
	public Integer getFrom() {
	    return from;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.ConstrainedSchedule#setTo(java.lang.String)
	 */
	public void setTo(Integer to) {
		this.to = to;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.treesched.ConstrainedSchedule#getTo()
	 */
	public Integer getTo() {
	    return to;
	}
	
	/**
     * @oddjob.property in
     * @oddjob.description The month in which this schedule is for. 
     * This has the same effect as setting from and to to the same thing.
     * @oddjob.required No.
	 * 
	 * @param in The in month.
	 */
	public void setIn(Integer in) {
		this.setFrom(in);
		this.setTo(in);
	}
	
    @Override
    protected CalendarUnit intervalBetween() {
    	return new CalendarUnit(Calendar.YEAR, 1);
    }    
		
	protected Calendar fromCalendar(Date referenceDate, TimeZone timeZone) {
		if (from == null) {
			return CalendarUtils.startOfYear(referenceDate, timeZone);
		}
		else {
				return CalendarUtils.monthOfYear(referenceDate,
						from,
						timeZone);
		}
	}
	
	protected Calendar toCalendar(Date referenceDate, TimeZone timeZone) {
		Calendar toCal = null;
	    if (to == null) {
			toCal = CalendarUtils.endOfYear(referenceDate, timeZone);
	    }
	    else {
	    	toCal = CalendarUtils.monthOfYear(referenceDate,
	    			to,
	    			timeZone);
	    }
	    
    	CalendarUtils.setEndOfMonth(toCal);
	    
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
