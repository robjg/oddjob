/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.schedules.units.WeekOfMonth;

public class CalendarUtils {

	private final Calendar baseCalendar;
	
	/**
	 * Constructor.
	 * 
	 * @param inDate The date to base functionality on.
	 * @param timeZone The timezone for the calendar
	 */
	public CalendarUtils(Date inDate, TimeZone timeZone) {
		baseCalendar = Calendar.getInstance(timeZone);
		baseCalendar.setTime(inDate);
	}

	/**
	 * Constructor base on an existing calendar.
	 * 
	 * @param baseCalendar
	 */
	public CalendarUtils(Calendar baseCalendar) {
		this(baseCalendar.getTime(), baseCalendar.getTimeZone());
	}

	/**
	 * Return a new calendar with the base calendar time zone
	 * but a different date.
	 * 
	 * @param date The date.
	 * @return The new calendar.
	 */
	public Calendar forDate(Date date) {

		Calendar c2 = Calendar.getInstance(baseCalendar.getTimeZone());
		c2.setTime(date);

		return c2;		
	}
	
	/**
	 * Calculate the start of day date time (i.e. at 00:00) for a given date.
	 * 
	 * @return The Calendar at the start of the day.
	 */
	public Calendar startOfDay() {
		
		Calendar c2 = Calendar.getInstance(baseCalendar.getTimeZone());
		c2.clear();
		c2.set(baseCalendar.get(Calendar.YEAR), 
				baseCalendar.get(Calendar.MONTH),
				baseCalendar.get(Calendar.DATE));

		return c2;		
	}
		
	/**
	 * Calculate start of the day after.
	 * 
	 * @return The Calendar at the start of the next day.
	 */
	public Calendar endOfDay() {
		
		Calendar c2 = Calendar.getInstance(baseCalendar.getTimeZone());
		c2.clear();
		c2.set(baseCalendar.get(Calendar.YEAR), 
				baseCalendar.get(Calendar.MONTH),
				baseCalendar.get(Calendar.DATE) + 1);

		return c2;		
	}
	
	/**
	 * Set the calendar to the end of day.
	 * 
	 * @param calendar The calendar that will be set.
	 */
	public static void setEndOfDay(Calendar calendar) {
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	/**
	 * Set the calendar to the end of the month.
	 * 
	 * @param calendar The calendar that will be set.
	 */
	public static void setEndOfMonth(Calendar calendar) {
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	/**
	 * Calculate the date at the start of the month for the given date.
	 * 
	 * @return The date at the start of the month.
	 */
	public Calendar startOfMonth() {
		
		Calendar c2 = Calendar.getInstance(baseCalendar.getTimeZone());
		c2.clear();
		c2.set(baseCalendar.get(Calendar.YEAR), 
				baseCalendar.get(Calendar.MONTH), 1);
		return c2;
	}

	/**
	 * Calculate the date at the end of the month for the given date.
	 * 
	 * @return The calendar at the end of the month.
	 */
	public Calendar endOfMonth() {
		
		// get the date at the beginning of next month
		Calendar c2 = Calendar.getInstance(baseCalendar.getTimeZone());
		c2.clear();
		c2.set(baseCalendar.get(Calendar.YEAR), 
				baseCalendar.get(Calendar.MONTH) + 1, 1);
		
		return c2;
	}
	
    /**
     * Utility function to get a calendar which 
     * represents the day of the month in which the reference date is.
     * 
     * @param day The day.
     * @return The Calendar.
     */
	public Calendar dayOfMonth(DayOfMonth dayOfMonth) {
		
		int day = dayOfMonth.getDayNumber();
		
		int month = day > 0 ? 
				baseCalendar.get(Calendar.MONTH) : 
					baseCalendar.get(Calendar.MONTH) + 1;
		
		Calendar c2 = Calendar.getInstance(
				baseCalendar.getTimeZone());
		c2.clear();
		c2.set(baseCalendar.get(Calendar.YEAR), month, day);
		
		return c2;
	}
		
	public Calendar startOfWeekOfMonth(WeekOfMonth week) {

		int weekNumber = week.getWeekNumber();
		
		Calendar result = Calendar.getInstance(baseCalendar.getTimeZone());
		result.clear();
		result.setFirstDayOfWeek(Calendar.MONDAY);
		result.setMinimalDaysInFirstWeek(7);
		result.set(Calendar.YEAR, baseCalendar.get(Calendar.YEAR));
		if (weekNumber < 0) {
			result.set(Calendar.MONTH, baseCalendar.get(Calendar.MONTH) + 1);
		}
		else {
			result.set(Calendar.MONTH, baseCalendar.get(Calendar.MONTH));
		}
		result.set(Calendar.WEEK_OF_MONTH, weekNumber);
		
		return result;
	}
	
	public Calendar endOfWeekOfMonth(WeekOfMonth week) {

		Calendar result = startOfWeekOfMonth(week);
		
		result.add(Calendar.DATE, 7);
		
		return result;		
	}
	
	public Calendar dayOfWeekInMonth(DayOfWeek dayOfWeek, WeekOfMonth week) {

		int weekNumber = week.getWeekNumber();
		int javaDay = javaDayOfWeek(dayOfWeek);
		
		Calendar result = Calendar.getInstance(baseCalendar.getTimeZone());
		result.clear();
		result.set(Calendar.YEAR, baseCalendar.get(Calendar.YEAR));
		result.set(Calendar.MONTH, baseCalendar.get(Calendar.MONTH));
		result.set(Calendar.DAY_OF_WEEK_IN_MONTH, weekNumber);
		result.set(Calendar.DAY_OF_WEEK, javaDay);
		
		return result;
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
	
	private static int javaDayOfWeek(DayOfWeek isoDayOfWeek) {
		
		switch (isoDayOfWeek.getDayNumber()) {
		case 1:
			return Calendar.MONDAY;
		case 2:
			return Calendar.TUESDAY;
		case 3:
			return Calendar.WEDNESDAY;
		case 4:
			return Calendar.THURSDAY;
		case 5:
			return Calendar.FRIDAY;
		case 6:
			return Calendar.SATURDAY;
		case 7:
			return Calendar.SUNDAY;
		default:
			throw new IllegalArgumentException("Invalid day of week " + 
					isoDayOfWeek);
		}
	}
	
    /**
     * Utility function to get a calendar which 
     * represents the day of the week from the reference date.

     * @param day The day.
     */
	public Calendar dayOfWeek(
			DayOfWeek dayOfWeek) {
		
		int day = dayOfWeek.getDayNumber();
				
		Calendar c2 = Calendar.getInstance(
				baseCalendar.getTimeZone());
		c2.clear();
		c2.set(baseCalendar.get(Calendar.YEAR), 
				baseCalendar.get(Calendar.MONTH), 
				baseCalendar.get(Calendar.DAY_OF_MONTH));
		
		int offset = day - isoDayOfWeek(baseCalendar.get(Calendar.DAY_OF_WEEK));
		
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
		
		return c2;
	}


	/**
	 * Calendar for the day of year.
	 * 
	 * @param referenceDate The date to take year from.
	 * @param dayOfYear The day of the year.
	 * @return The calendar.
	 */
	public Calendar dayOfYear(int dayOfMonth, int month) {
		
	    Calendar c2 = Calendar.getInstance(baseCalendar.getTimeZone());
	    c2.clear();
	    c2.set(Calendar.YEAR, baseCalendar.get(Calendar.YEAR));
	    c2.set(Calendar.DATE, dayOfMonth);
		c2.set(Calendar.MONTH, month - 1);
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
