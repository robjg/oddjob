/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalendarUtils {

	/**
	 * Set the calendar to the end of day.
	 * 
	 * @param calendar The calendar that will be set.
	 */
	public static void setEndOfDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
	}
	
	/**
	 * Set the calendar to the end of the month.
	 * 
	 * @param calendar The calendar that will be set.
	 */
	public static void setEndOfMonth(Calendar calendar) {
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
	}
	
	/**
	 * Calculate the date at the start of the month for the given date.
	 * 
	 * @param inDate The given date.
	 * @return The date at the start of the month.
	 */
	public static Calendar startOfMonth(Date inDate, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(inDate);
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		c2.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), 1);
		return c2;
	}

	/**
	 * Calculate the date at the end of the month for the given date.
	 * 
	 * @param inDate The given date.
	 * @return The calendar at the end of the month.
	 */
	public static Calendar endOfMonth(Date inDate, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(inDate);
		
		// get the date at the beginning of next month
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		c2.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH) + 1, 1);
		// and take off a millisecond.
		c2.add(Calendar.MILLISECOND, -1);
		return c2;
	}
	
    /**
     * Utility function to get a calendar which 
     * represents the day of the month in which the reference date is.
     * 
     * @param referenceDate The date to take month from.
     * @param day The day.
     * @return The Calendar.
     */
	public static Calendar dayOfMonth(Date referenceDate, int day, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(referenceDate);
		
		int month = day > 0 ? c1.get(Calendar.MONTH) : c1.get(Calendar.MONTH) + 1;
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		c2.set(c1.get(Calendar.YEAR), month, day);
		return c2;
	}
		
	/**
	 * Calculate the start of the week for the given date.
	 * 
	 * @param inDate The given date.
	 * 
	 * @return The date at the start of the month.
	 */
	public static Calendar startOfWeek(Date inDate, TimeZone timeZone) {

		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(inDate);
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		
		int daysBefore = isoDayOfWeek(c1.get(Calendar.DAY_OF_WEEK)) - 1;
		
		c2.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), 
				c1.get(Calendar.DAY_OF_MONTH) - daysBefore);
		return c2;		
	}

	/**
	 * Calculate the start of the week for the given date.
	 * 
	 * @param inDate The given date.
	 * 
	 * @return The date at the start of the month.
	 */
	public static Calendar endOfWeek(Date inDate, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(inDate);
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		
		int daysToAdd = 8 - isoDayOfWeek(c1.get(Calendar.DAY_OF_WEEK));
		
		c2.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), 
				c1.get(Calendar.DAY_OF_MONTH) + daysToAdd);
		
		c2.add(Calendar.MILLISECOND, -1);
		
		return c2;
	}
	
	private static int isoDayOfWeek(int javaDayOfWeek) {
		
		switch (javaDayOfWeek) {
		case Calendar.MONDAY:
			return 1;
		case Calendar.TUESDAY:
			return 2;
		case Calendar.WEDNESDAY:
			return 3;
		case Calendar.THURSDAY:
			return 4;
		case Calendar.FRIDAY:
			return 5;
		case Calendar.SATURDAY:
			return 6;
		case Calendar.SUNDAY:
			return 7;
		default:
			throw new IllegalArgumentException("Invalid day of week " + 
					javaDayOfWeek);
		}
	}
	
    /**
     * Utility function to get a calendar which 
     * represents the day of the week from the reference date.

     * @param referenceDate The date to take week from.
     * @param day The day.
     * @return The Calendar.
     */
	public static Calendar dayOfWeek(Date referenceDate, int day, TimeZone timeZone) {
		
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(referenceDate);
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		c2.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), 
				c1.get(Calendar.DAY_OF_MONTH));
		
		int offset = day - isoDayOfWeek(c1.get(Calendar.DAY_OF_WEEK));
		
		c2.add(Calendar.DATE, offset);
		
		return c2;
	}
	
	/**
	 * Calculate the date at the start of the year for the given date.
	 * 
	 * @param referenceDate The given date.
	 * @return The calendar at the start of the year.
	 */
	public static Calendar startOfYear(Date referenceDate, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(referenceDate);
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		c2.set(c1.get(Calendar.YEAR), 0, 1);
		return c2;		
	}

	/**
	 * Calcuate the date at the end of the year from the
	 * given date.
	 * 
	 * @param referenceDate The given date.
	 * @return The calendar at the end of the year.
	 */
	public static Calendar endOfYear(Date referenceDate, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(referenceDate);
		
		Calendar c2 = Calendar.getInstance();
		c2.clear();
		c2.set(c1.get(Calendar.YEAR) + 1, 0, 1);
		c2.setTimeZone(timeZone);
		// and take off a millisecond.
		c2.add(Calendar.MILLISECOND, -1);
		return c2;
	}


	/**
	 * Calendar for the day of year.
	 * 
	 * @param referenceDate The date to take year from.
	 * @param dayOfYear The day of the year.
	 * @return The calendar.
	 */
	public static Calendar dayOfYear(Date referenceDate, int dayOfYear, TimeZone timeZone) {
		Calendar c1 = Calendar.getInstance();
		c1.setTimeZone(timeZone);
		c1.setTime(referenceDate);
		
	    Calendar c2 = Calendar.getInstance(timeZone);
	    c2.clear();
	    c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
		c2.set(Calendar.DAY_OF_YEAR, dayOfYear);
		return c2;
	}

	/**
	 * Utility function to get the start of the month as a Calendar.
	 * 
	 * @param referenceDate The whole date.
	 * @param month The month to calculate the start of. 1 is January, 
	 * 		12 is December.
	 * 
	 * @return The start of the month.
	 */
	public static Calendar monthOfYear(Date referenceDate, int month, TimeZone timeZone) {		
		Calendar c1 = Calendar.getInstance(timeZone);
		c1.setTime(referenceDate);
		
		Calendar c2 = Calendar.getInstance(timeZone);
		c2.clear();
		
		// Adjust for Java 0 based months.
		c2.set(c1.get(Calendar.YEAR), month - 1, 1);
		
		return c2;
	}

}
