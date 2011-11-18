/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.schedules.units.WeekOfMonth;

public class CalendarUtilsTest extends TestCase {

	public void testAddYearAssumption() throws ParseException {
		
		Calendar test = Calendar.getInstance();
		test.clear();
		test.set(Calendar.DAY_OF_MONTH, 0);
		test.set(Calendar.MONTH, 2);
		test.set(Calendar.YEAR, 2003);

		assertEquals(DateHelper.parseDate("2003-02-28"), test.getTime());
		
		test.add(Calendar.YEAR, 1);
		
		// Note that adding a year to the last day of the month isn't the
		// the last day of the month a year later!!!
		assertEquals(DateHelper.parseDate("2004-02-28"), test.getTime());
		
		test.clear();
		test.set(Calendar.DAY_OF_MONTH, 0);
		test.set(Calendar.MONTH, 2);
		test.set(Calendar.YEAR, 2004);

		assertEquals(DateHelper.parseDate("2004-02-29"), test.getTime());
		
		test.add(Calendar.YEAR, 1);
		
		// Works the other way though.
		assertEquals(DateHelper.parseDate("2005-02-28"), test.getTime());
	}
	
	public void testSetEndOfDay() throws ParseException {
		Calendar test = Calendar.getInstance();
		test.setTime(DateHelper.parseDateTime("2003-07-15 12:27"));
		CalendarUtils.setEndOfDay(test);
		
		assertEquals(DateHelper.parseDateTime("2003-07-16 00:00"), 
				test.getTime());
	}
	
	public void testSetEndOfMonth() throws ParseException {
		Calendar test = Calendar.getInstance();
		test.setTime(DateHelper.parseDateTime("2003-07-15 12:27"));
		CalendarUtils.setEndOfMonth(test);
		
		assertEquals(DateHelper.parseDateTime("2003-08-01 00:00"), 
				test.getTime());
	}
	
	/*
	 * Test for Date startOfMonth(Date)
	 */
	public void testStartOfMonthDate() throws ParseException {
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2003-07-11 12:27"), 
				TimeZone.getDefault());
		
		assertEquals(DateHelper.parseDateTime("2003-07-01"), 
				test.startOfMonth().getTime());
	}


	/*
	 * Test for Date endOfMonth(Date)
	 */
	public void testEndOfMonthDate() throws ParseException {
		
		CalendarUtils test = new CalendarUtils(
						DateHelper.parseDateTime("2004-02-11 12:27"), 
						TimeZone.getDefault());
		
		assertEquals(DateHelper.parseDateTime("2004-03-01 00:00"), 
				test.endOfMonth().getTime());
	}
	
	/*
	 * Test for Date endOfMonth(Date)
	 */
	public void testDayOfMonth() throws ParseException {
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2003-07-11 12:27"),
				TimeZone.getDefault());
		
		Calendar result = test.dayOfMonth(
				new DayOfMonth.Number(15));
		
		assertEquals(DateHelper.parseDateTime("2003-07-15"), 
				result.getTime());
	}
	
	public void testStartOfWeekDate() throws ParseException {

		Calendar result = CalendarUtils.startOfWeek(
				DateHelper.parseDateTime("2006-03-08 12:27"), 
				TimeZone.getDefault());
		
		Date expected = DateHelper.parseDateTime("2006-03-06");
		
		assertEquals(expected, result.getTime());
		
		result = CalendarUtils.startOfWeek(
				DateHelper.parseDateTime("2011-03-01 12:27"), 
				TimeZone.getDefault());
		
		expected = DateHelper.parseDateTime("2011-02-28");
		
		assertEquals(expected, result.getTime());
		
		result = CalendarUtils.startOfWeek(
				DateHelper.parseDateTime("2011-01-01 12:27"), 
				TimeZone.getDefault());
		
		expected = DateHelper.parseDateTime("2010-12-27");
		
		assertEquals(expected, result.getTime());
	}


	public void testEndOfWeekDate() throws ParseException {
		
		Calendar result = CalendarUtils.endOfWeek(
				DateHelper.parseDateTime("2006-03-08 12:27"), 
				TimeZone.getDefault());
		
		Date expected = DateHelper.parseDateTime(
				"2006-03-13 00:00");
		
		assertEquals(expected, result.getTime());
		
		result = CalendarUtils.endOfWeek(
				DateHelper.parseDateTime("2010-12-27 00:00"), 
				TimeZone.getDefault());
		
		expected = DateHelper.parseDateTime(
				"2011-01-03 00:00");
		
		assertEquals(expected, result.getTime());
	}
	
	public void testDayOfWeek() throws ParseException {

		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2006-03-09 12:27"), 
				TimeZone.getDefault());
		
		// 9th of March 2006 was a Thursday.
		Calendar result = test.dayOfWeek(
				DayOfWeek.Days.WEDNESDAY);
		
		Date expected = DateHelper.parseDateTime("2006-03-08");
		
		assertEquals(expected, result.getTime());
		
		result = test.dayOfWeek(
				DayOfWeek.Days.SATURDAY);
		
		expected = DateHelper.parseDateTime("2006-03-11");
	}
	
	public void testStartOfYearDate() throws ParseException {
		assertEquals(DateHelper.parseDate("2003-01-01"),
				CalendarUtils.startOfYear(
						DateHelper.parseDateTime("2003-07-11 12:27"), 
						TimeZone.getDefault()).getTime());
	}


	public void testEndOfYearDate() throws ParseException {
		assertEquals(DateHelper.parseDateTime("2004-01-01 00:00"),
				CalendarUtils.endOfYear(
						DateHelper.parseDateTime("2003-06-11 12:27"), 
						TimeZone.getDefault()).getTime());
	}

	public void testDayOfYear() throws ParseException {
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2008-02-09 12:27"),
				TimeZone.getDefault());
		
		Calendar result = test.dayOfYear(26, 5);
		
		Date expected = DateHelper.parseDateTime("2008-05-26");
		
		assertEquals(expected, result.getTime());
	}
	
	public void testMonthOfYear() throws ParseException {
		
		assertEquals(DateHelper.parseDateTime("2006-03-01"), 
				CalendarUtils.monthOfYear(
						DateHelper.parseDateTime("2006-07-09 12:27"), 3, 
						TimeZone.getDefault()).getTime());
	}
	
	public void testStartOfWeekOfMonth() throws ParseException{
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2011-02-18 12:45"), 
				TimeZone.getDefault());
		
		Calendar result = test.startOfWeekOfMonth(
				WeekOfMonth.Weeks.SECOND);
		
		assertEquals(
				DateHelper.parseDate("2011-02-14"),
				result.getTime());
		
		result = test.startOfWeekOfMonth(
				WeekOfMonth.Weeks.LAST);
		
		assertEquals(
				DateHelper.parseDate("2011-02-21"),
				result.getTime());
		
		result = test.startOfWeekOfMonth(
				new WeekOfMonth.Number(0));
		
		assertEquals(
				DateHelper.parseDate("2011-01-31"),
				result.getTime());
		
		result = test.startOfWeekOfMonth(
				WeekOfMonth.Weeks.FOURTH);
		
		assertEquals(
				DateHelper.parseDate("2011-02-28"),
				result.getTime());
	}
	
	public void testEndOfWeekOfMonth() throws ParseException{
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2011-02-18 12:45"), 
				TimeZone.getDefault());
		
		Calendar result = test.endOfWeekOfMonth(
				WeekOfMonth.Weeks.SECOND);
		
		assertEquals(
				DateHelper.parseDate("2011-02-21"),
				result.getTime());
		
		result = test.endOfWeekOfMonth(
				WeekOfMonth.Weeks.LAST);
		
		assertEquals(
				DateHelper.parseDate("2011-02-28"),
				result.getTime());
		
		result = test.endOfWeekOfMonth(
				new WeekOfMonth.Number(0));
		
		assertEquals(
				DateHelper.parseDate("2011-02-07"),
				result.getTime());
		
		result = test.endOfWeekOfMonth(
				WeekOfMonth.Weeks.FOURTH);
		
		assertEquals(
				DateHelper.parseDate("2011-03-07"),
				result.getTime());
	}
	
	public void testStartOfDayOfWeekOfMonth() throws ParseException{
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2011-06-15 12:45"), 
				TimeZone.getDefault());
		
		Calendar result = test.dayOfWeekInMonth(
				DayOfWeek.Days.FRIDAY, WeekOfMonth.Weeks.FIRST);
		
		assertEquals(
				DateHelper.parseDate("2011-06-03"),
				result.getTime());
		
		result = test.dayOfWeekInMonth(
				DayOfWeek.Days.FRIDAY, WeekOfMonth.Weeks.SECOND);
		
		assertEquals(
				DateHelper.parseDate("2011-06-10"),
				result.getTime());
		
		result = test.dayOfWeekInMonth(
				DayOfWeek.Days.FRIDAY, WeekOfMonth.Weeks.LAST);
		
		assertEquals(
				DateHelper.parseDate("2011-06-24"),
				result.getTime());
		
		result = test.dayOfWeekInMonth(
				DayOfWeek.Days.FRIDAY, new WeekOfMonth.Number(0));
		
		assertEquals(
				DateHelper.parseDate("2011-05-27"),
				result.getTime());

		result = test.dayOfWeekInMonth(
				DayOfWeek.Days.FRIDAY, WeekOfMonth.Weeks.PENULTIMATE);
		
		assertEquals(
				DateHelper.parseDate("2011-06-17"),
				result.getTime());
		
		result = test.dayOfWeekInMonth(
				DayOfWeek.Days.FRIDAY, WeekOfMonth.Weeks.FITH);
		
		assertEquals(
				DateHelper.parseDate("2011-07-01"),
				result.getTime());		
	}
	
	public void testStartOfDay() throws ParseException {
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2011-06-15 12:45"), 
				TimeZone.getDefault());
		
		Calendar result = test.startOfDay();
		
		assertEquals(DateHelper.parseDateTime("2011-06-15 00:00"),
				result.getTime());
	}
		
	public void testEndOfDay() throws ParseException {
		
		CalendarUtils test = new CalendarUtils(
				DateHelper.parseDateTime("2011-06-15 12:45"), 
				TimeZone.getDefault());
		
		Calendar result = test.endOfDay();
		
		assertEquals(DateHelper.parseDateTime("2011-06-16 00:00"),
				result.getTime());		
	}
}
