package org.oddjob.schedules;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Utility methods to do things with dates and intervals.
 * 
 * @author Rob Gordon
 */

public class DateUtils {

	/**
	 * Private constructor as instantiating this utility class 
	 * is meaningless.
	 */
	private DateUtils() {
	}

	/**
	 * Calculate the start of day date time (i.e. at 00:00) for a given date.
	 * 
	 * @param inDate The date to cacluate start of day from.
	 * @return The Date at the start of the day.
	 */
	public static Date startOfDay(Date inDate, TimeZone timeZone) {
		GregorianCalendar c1 = new GregorianCalendar();
		c1.setTimeZone(timeZone);
		c1.setTime(inDate);
		GregorianCalendar c2 = new GregorianCalendar(
				c1.get(Calendar.YEAR), c1.get(Calendar.MONTH),
				c1.get(Calendar.DATE));
		c2.setTimeZone(timeZone);
		return c2.getTime();		
	}
		
	/**
	 * Calculate the date time at the end of the day (one millisecond before midnight)
	 * for the given date.
	 * 
	 * @param inDate The given date.
	 * @return The Date at the end of the day.
	 */
	public static Date endOfDay(Date inDate, TimeZone timeZone) {
		Calendar inCalendar = new GregorianCalendar();
		inCalendar.setTimeZone(timeZone);
		inCalendar.setTime(inDate);
		Calendar nextDay = new GregorianCalendar(
		        inCalendar.get(Calendar.YEAR),
		        inCalendar.get(Calendar.MONTH),
		        inCalendar.get(Calendar.DAY_OF_MONTH) + 1);
		nextDay.setTimeZone(timeZone);
		return new Date(nextDay.getTime().getTime());
	}
	
	/**
	 * Calculate the day number for the given date.
	 * 
	 * @param inDate The given date.
	 * @return The day number.
	 */
	public static int dayOfWeek(Date inDate, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(inDate);

		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * Calcuate the month number (0 - 11) for the given date.
	 * 
	 * @param inDate The given date.
	 * @return The month number.
	 */	
	public static int month(Date inDate, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTime(inDate);

		return calendar.get(Calendar.MONTH);
	}
	
	/**
	 * Calculate the day of the month (1 - 31) for the given date.
	 * 
	 * @param inDate The given date.
	 * @return The day of the month.
	 */
	public static int dayOfMonth(Date inDate, TimeZone timeZone) {	
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTime(inDate);

		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * Calculate the day of the year for the given
	 * date.
	 * 
	 * @param forDate The date.
	 * @return The day of the year.
	 */
	public static int dayOfYear(Date forDate, TimeZone timeZone) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeZone(timeZone);
	    calendar.setTime(forDate);
	    return calendar.get(Calendar.DAY_OF_YEAR);
	}
			
	/**
	 * Return a date which is 1 millisecond after the given date.
	 * 
	 * @param date The given date.
	 * @return The date one millisecond later.
	 */	
	public static Date oneMillisAfter(Date date) {
		if (date.equals(Interval.END_OF_TIME)) {
			return null;
		}
		return new Date(date.getTime() + 1);
	}
	
	/**
	 * Return a date which is 1 millisecond before the given date.
	 * 
	 * @param date The given date.
	 * @return The date one millisecond before.
	 */	
	public static Date oneMillisBefore(Date date) {
		return new Date(date.getTime() - 1);
	}

	/**
	 * Compare to calendars.
	 * 
	 * @param c1 First calendar.
	 * @param c2 Second calendar
	 * @return 1 if c1 > c2, 0 if c1 = c2, -1 if c1 < c2.
	 */	
	public static int compare(Calendar c1, Calendar c2) {
		long m1 = c1.getTime().getTime();
		long m2 = c2.getTime().getTime();
		
		if (m1  < m2) {
			return -1;
		}
		if (m1 > m2) {
			return 1;
		}
		return 0;
	}
}

