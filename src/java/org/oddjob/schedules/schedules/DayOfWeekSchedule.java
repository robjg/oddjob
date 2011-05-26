package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.CalendarUtils;
import org.oddjob.schedules.ConstrainedSchedule;

/**
 * @oddjob.description A schedule for the days of the week. This schedule
 * will typically be used with a {@link TimeSchedule} refinement property.
 * <p>
 * The days of the week are specified according to the ISO 8601 standard
 * with Monday being day 1 and Sunday being day 7.
 * 
 * @oddjob.example A schedule for all day Tuesday. This schedule defines an
 * interval between midnight Tuesday morning and up to midnight Tuesday night.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfWeekOnExample.xml}
 * 
 * @oddjob.example A schedule between Friday and the following Monday inclusive. 
 * This schedule is refined by a time that will define the schedule to be each
 * of the days Friday, Saturday, Sunday, Monday at 3:45pm.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfWeekBetweenExample.xml}
 * 
 * @author Rob Gordon
 */


final public class DayOfWeekSchedule extends ConstrainedSchedule 
implements Serializable {

    private static final long serialVersionUID = 20050226;
    
	/** The from day int as a string. */
	private Integer from;
	
	/** The to day int as a string. */
	private Integer to;
	
    /**
     * @oddjob.property from
     * @oddjob.description The from day of the week.
     * @oddjob.required No. Defaults to Monday.
     * 
     * @param from The from date.
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
	
    /**
     * @oddjob.property to
     * @oddjob.description The to day of the week.
     * @oddjob.required No. Defaults to Sunday.
     * 
     * @param to The to day.
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
     * @oddjob.property on
     * @oddjob.description The on day of week. This has the same effect as
     * setting from and to to the same thing.
     * @oddjob.required No.
	 * 
	 * @param on The on text.
	 */
	public void setOn(Integer on) {		
		setFrom(on);
		setTo(on);
	}
	
    @Override
    protected CalendarUnit intervalBetween() {
    	return new CalendarUnit(Calendar.DATE, 7);
    }
	
	protected Calendar fromCalendar(Date referenceDate, TimeZone timeZone) {
		if (from == null) {
			return CalendarUtils.startOfWeek(referenceDate, timeZone);
		}
		else {
			return CalendarUtils.dayOfWeek(referenceDate,
					from,
					timeZone);
		}
	}
	
	protected Calendar toCalendar(Date referenceDate, TimeZone timeZone) {
	    if (to == null) {
			return CalendarUtils.endOfWeek(referenceDate, timeZone);
	    }
	    else {
	    	Calendar cal = CalendarUtils.dayOfWeek(referenceDate,
	    			to,
	    			timeZone);
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
