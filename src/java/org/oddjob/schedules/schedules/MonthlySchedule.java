package org.oddjob.schedules.schedules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.schedules.CalendarUnit;
import org.oddjob.schedules.CalendarUtils;
import org.oddjob.schedules.ConstrainedSchedule;
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.schedules.units.WeekOfMonth;

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


final public class MonthlySchedule extends ConstrainedSchedule implements Serializable {

    private static final long serialVersionUID = 20050226;
    
    /** from day of month. */
    private DayOfMonth fromDay;
    
    /** to day of month. */
    private DayOfMonth toDay;
        
    private DayOfWeek fromDayOfWeek;
    
    private DayOfWeek toDayOfWeek;
    
    private WeekOfMonth fromWeek;
    
    private WeekOfMonth toWeek;
    
    /**
     * @oddjob.property from
     * @oddjob.description The from day of the month.
     * @oddjob.required No. Defaults to 1.
     * 
     * @param from The from date.
     */
    @ArooaAttribute
    public void setFromDay(DayOfMonth day) {
    	fromDay = day;
    }
    
    public DayOfMonth getFromDay() {
        return fromDay;
    }
    
    /**
     * @oddjob.property to
     * @oddjob.description The to day of the month.
     * @oddjob.required No. Defaults to the last day of the month.
     * 
     * @param to The to date.
     */
    @ArooaAttribute
    public void setToDay(DayOfMonth to) {
    	this.toDay = to;
    }
    
    public DayOfMonth getToDay() {
        return toDay;
    }

	/**
     * @oddjob.property on
     * @oddjob.description The day on which this schedule is for. 
     * This has the same effect as setting from and to to the same thing.
     * @oddjob.required No.
	 * 
	 * @param on The day on which this schedule is for.
	 */
    @ArooaAttribute
	public void setOnDay(DayOfMonth on) {
		this.setFromDay(on);
		this.setToDay(on);
	}
	
    public DayOfWeek getFromDayOfWeek() {
		return fromDayOfWeek;
	}

    @ArooaAttribute
	public void setFromDayOfWeek(DayOfWeek fromDayOfWeek) {
		this.fromDayOfWeek = fromDayOfWeek;
	}

	public DayOfWeek getToDayOfWeek() {
		return toDayOfWeek;
	}

    @ArooaAttribute
	public void setToDayOfWeek(DayOfWeek toDayOfWeek) {
		this.toDayOfWeek = toDayOfWeek;
	}

    @ArooaAttribute
    public void setOnDayOfWeek(DayOfWeek onDayOfWeek) {
    	this.setFromDayOfWeek(onDayOfWeek);
    	this.setToDayOfWeek(onDayOfWeek);
    }
    
	public WeekOfMonth getFromWeek() {
		return fromWeek;
	}

    @ArooaAttribute
	public void setFromWeek(WeekOfMonth fromWeek) {
		this.fromWeek = fromWeek;
	}

	public WeekOfMonth getToWeek() {
		return toWeek;
	}

    @ArooaAttribute
	public void setToWeek(WeekOfMonth toWeek) {
		this.toWeek = toWeek;
	}

    @ArooaAttribute
    public void setInWeek(WeekOfMonth inWeek) {
    	this.setFromWeek(inWeek);
    	this.setToWeek(inWeek);
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

		CalendarUtils helper = new CalendarUtils(
				referenceDate, timeZone);
		
		if (fromDay != null) {
			return helper.dayOfMonth(fromDay);
	    }
		else if (fromWeek != null) {
			
			if (fromDayOfWeek == null){
				return helper.startOfWeekOfMonth(fromWeek);
			}
			else {
				return helper.dayOfWeekInMonth(fromDayOfWeek, fromWeek);
			}
		}
		else {		
	    	return helper.startOfMonth();
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
		
		CalendarUtils helper = new CalendarUtils(
				referenceDate, timeZone);
		
	    if (toDay != null) {
	    	Calendar cal = helper.dayOfMonth(toDay);
	    	CalendarUtils.setEndOfDay(cal);
	    	return cal;
	    }
	    else if (toWeek != null) {

	    	if (toDayOfWeek == null) {
	    		return helper.startOfWeekOfMonth(toWeek);	    		
	    	}
	    	else {
	    		Calendar cal = helper.dayOfWeekInMonth(
	    				toDayOfWeek, toWeek);
	    		CalendarUtils.setEndOfDay(cal);
	    		return cal;
	    	}
	    }
	    else {		
	    	return helper.endOfMonth();
	    }
	}	
		
	@Override
	protected Calendar shiftFromCalendar(Calendar calendar, int intervals) {
		
    	calendar = super.shiftFromCalendar(calendar, intervals);
    	
	    if (fromWeek == null) {
	    	return calendar;
	    }
	    else {
			CalendarUtils helper = new CalendarUtils(
					calendar.getTime(), calendar.getTimeZone());
			
	    	if (fromDayOfWeek == null) {
	    		return helper.startOfWeekOfMonth(fromWeek);	    		
	    	}
	    	else {
	    		return helper.dayOfWeekInMonth(
	    				fromDayOfWeek, fromWeek);
	    	}
	    }
	}
	
	@Override
	protected Calendar shiftToCalendar(Calendar calendar, int intervals) {
		
    	calendar = super.shiftFromCalendar(calendar, intervals);
    	
	    if (toWeek == null) {
	    	return calendar;
	    }
	    else {
			CalendarUtils helper = new CalendarUtils(
					calendar.getTime(), calendar.getTimeZone());
			
	    	if (toDayOfWeek == null) {
	    		return helper.startOfWeekOfMonth(toWeek);	    		
	    	}
	    	else {
	    		Calendar cal = helper.dayOfWeekInMonth(
	    				toDayOfWeek, toWeek);
	    		CalendarUtils.setEndOfDay(cal);
	    		return cal;
	    	}
	    }
	}
	
	/**
	 * Override toString.
	 * 
	 * @return A description of the schedule.
	 */
	public String toString() {
		
		String from = null;
		
		if (fromDay != null) {
			from = "day " + fromDay.toString();
		}
		else if (fromWeek != null) {
			if (fromDayOfWeek == null) {
				from = "week " + fromWeek.toString();
			}
			else {
				from = "week " + fromWeek.toString() + ", day " + fromDayOfWeek.toString();
			}
		}
		else {
			from = "the start of the month";
		}
		
		String to = null;
		
		if (toDay != null) {
			to = "day " + toDay.toString();
		}
		else if (toWeek != null) {
			if (toDayOfWeek == null) {
				to = "week " + toWeek.toString();
			}
			else {
				to = "week " + toWeek.toString() + ", day " + toDayOfWeek.toString();
			}
		}
		else {
			to = "the end of the month";
		}
		
		StringBuilder description = new StringBuilder();
		if (from.equals(to)) {
			description.append(" on ");
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
		
		return "Monthly"  + description.toString();
	}
}
