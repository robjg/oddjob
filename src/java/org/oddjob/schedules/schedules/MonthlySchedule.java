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
 * @oddjob.description A schedule for monthly intervals. The intervals
 * can be specified as days of the month, a day of the week in a week of the month,
 * or less usefully as weeks of the month. 
 * <p>
 * The day of the month is given
 * as an number, normally 1 to 31. 0 and negative numbers can be used to specify
 * days from the end of the month. The words LAST and PENULTIMATE 
 * (case insensitive) can also be
 * used as a convenience. Note that unlike the java 
 * <code>GregorianCalander</code>, 0 and negative numbers are taken to be
 * this month, not the previous month. i.e. on="0" is the last day of the month and
 * is equivalent to on="LAST".
 * <p>
 * Days and week of the month are given as the day number, 1 to 7, or as one
 * of MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY 
 * (case insensitive). The week of 
 * the month is specified as a number, typically 1 to 5, or using one of FIRST,
 * SECOND, THIRD, FOURTH, FITH, PENULTIMATE, or LAST (case insensitive).
 * <p>
 * If the week of the month is specified on it's own then the first week is
 * taken to be the first complete week of the month.
 * 
 * 
 * @oddjob.example
 * 
 * A range of days of the month.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfMonthExample1.xml}
 * 
 * This would schedule a job to run every day from the 17th of each month to
 * the 25th of each month at 10am.
 * 
 * @oddjob.example
 * 
 * On a single day of the month.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/DayOfMonthExample2.xml}
 * 
 * This will run a job on the 15th of every month.
 * 
 * @oddjob.example
 * 
 * On the last Friday of the month.
 * 
 * {@oddjob.xml.resource org/oddjob/schedules/schedules/LastFridayOfMonth.xml}
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
     * @oddjob.property fromDay
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
     * @oddjob.property toDay
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
     * @oddjob.property onDay
     * @oddjob.description The day on which this schedule is for. 
     * This has the same effect as setting <code>fromDay</code>
     * and <code>toDay</code> to the same thing.
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

    /**
     * @oddjob.property fromDayOfWeek
     * @oddjob.description The from day of the week. Used in conjunction with
     * <code>fromWeekOfMonth</code>.
     * @oddjob.required No.
     * 
     * @param from The from date.
     */
    @ArooaAttribute
	public void setFromDayOfWeek(DayOfWeek fromDayOfWeek) {
		this.fromDayOfWeek = fromDayOfWeek;
	}

	public DayOfWeek getToDayOfWeek() {
		return toDayOfWeek;
	}

    /**
     * @oddjob.property toDayOfWeek
     * @oddjob.description The to day of the week. Used in conjunction with
     * <code>toDayOfWeek</code>.
     * @oddjob.required No.
     * 
     * @param from The from date.
     */
    @ArooaAttribute
	public void setToDayOfWeek(DayOfWeek toDayOfWeek) {
		this.toDayOfWeek = toDayOfWeek;
	}

    /**
     * @oddjob.property onDayOfWeek
     * @oddjob.description The on day of the week. This is equivalent to 
     * setting <code>fromDayOfWeek</code> and  <code>toDayOfWeek</code>
     * to the same thing.
     * @oddjob.required No.
     * 
     * @param from The from date.
     */
    @ArooaAttribute
    public void setOnDayOfWeek(DayOfWeek onDayOfWeek) {
    	this.setFromDayOfWeek(onDayOfWeek);
    	this.setToDayOfWeek(onDayOfWeek);
    }
    
	public WeekOfMonth getFromWeek() {
		return fromWeek;
	}

    /**
     * @oddjob.property fromWeek
     * @oddjob.description The from week of the month.
     * @oddjob.required No.
     * 
     * @param from The from date.
     */
    @ArooaAttribute
	public void setFromWeek(WeekOfMonth fromWeek) {
		this.fromWeek = fromWeek;
	}

	public WeekOfMonth getToWeek() {
		return toWeek;
	}

    /**
     * @oddjob.property toWeek
     * @oddjob.description The to week of the month.
     * @oddjob.required No.
     * 
     * @param from The from date.
     */
    @ArooaAttribute
	public void setToWeek(WeekOfMonth toWeek) {
		this.toWeek = toWeek;
	}

    /**
     * @oddjob.property inWeek
     * @oddjob.description The in week of the month. This is equivalent to 
     * setting <code>fromWeek</code> and <code>toWeek</code> to the same thing.
     * @oddjob.required No.
     * 
     * @param from The from date.
     */
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
		
    	calendar = super.shiftToCalendar(calendar, intervals);
    	
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
