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
		
		assertEquals(DateHelper.parseDateTime("2003-07-15 23:59:59:999"), 
				test.getTime());
	}
	
	public void testSetEndOfMonth() throws ParseException {
		Calendar test = Calendar.getInstance();
		test.setTime(DateHelper.parseDateTime("2003-07-15 12:27"));
		CalendarUtils.setEndOfMonth(test);
		
		assertEquals(DateHelper.parseDateTime("2003-07-31 23:59:59:999"), 
				test.getTime());
	}
	
	/*
	 * Test for Date startOfMonth(Date)
	 */
	public void testStartOfMonthDate() throws ParseException {
		assertEquals(DateHelper.parseDateTime("2003-07-01"), 
				CalendarUtils.startOfMonth(
						DateHelper.parseDateTime("2003-07-11 12:27"), 
						TimeZone.getDefault()).getTime());
	}


	/*
	 * Test for Date endOfMonth(Date)
	 */
	public void testEndOfMonthDate() throws ParseException {
		assertEquals(DateHelper.parseDateTime("2004-02-29 23:59:59:999"), 
				CalendarUtils.endOfMonth(
						DateHelper.parseDateTime("2004-02-11 12:27"), 
						TimeZone.getDefault()).getTime());
	}
	
	/*
	 * Test for Date endOfMonth(Date)
	 */
	public void testDayOfMonth() throws ParseException {
		assertEquals(DateHelper.parseDateTime("2003-07-15"), 
				CalendarUtils.dayOfMonth(
						DateHelper.parseDateTime("2003-07-11 12:27"), 15, 
						TimeZone.getDefault()).getTime());
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
				"2006-03-12 23:59:59:999");
		
		assertEquals(expected, result.getTime());
		
		result = CalendarUtils.endOfWeek(
				DateHelper.parseDateTime("2010-12-27 00:00"), 
				TimeZone.getDefault());
		
		expected = DateHelper.parseDateTime(
				"2011-01-02 23:59:59:999");
		
		assertEquals(expected, result.getTime());
	}
	
	public void testDayOfWeek() throws ParseException {

		// 9th of March 2006 was a Thursday.
		Calendar result = CalendarUtils.dayOfWeek(
				DateHelper.parseDateTime("2006-03-09 12:27"), 3, 
				TimeZone.getDefault());
		
		Date expected = DateHelper.parseDateTime("2006-03-08");
		
		assertEquals(expected, result.getTime());
		
		result = CalendarUtils.dayOfWeek(
				DateHelper.parseDateTime("2006-03-09 12:27"), 6, 
				TimeZone.getDefault());
		
		expected = DateHelper.parseDateTime("2006-03-11");
	}
	
	public void testStartOfYearDate() throws ParseException {
		assertEquals(DateHelper.parseDate("2003-01-01"),
				CalendarUtils.startOfYear(
						DateHelper.parseDateTime("2003-07-11 12:27"), 
						TimeZone.getDefault()).getTime());
	}


	public void testEndOfYearDate() throws ParseException {
		assertEquals(DateHelper.parseDateTime("2003-12-31 23:59:59:999"),
				CalendarUtils.endOfYear(
						DateHelper.parseDateTime("2003-06-11 12:27"), 
						TimeZone.getDefault()).getTime());
	}

	public void testDayOfYear() throws ParseException {
		
		assertEquals(DateHelper.parseDateTime("2006-03-08"), 
				CalendarUtils.dayOfYear(
						DateHelper.parseDateTime("2006-02-09 12:27"), 67, 
						TimeZone.getDefault()).getTime());
	}
	
	public void testMonthOfYear() throws ParseException {
		
		assertEquals(DateHelper.parseDateTime("2006-03-01"), 
				CalendarUtils.monthOfYear(
						DateHelper.parseDateTime("2006-07-09 12:27"), 3, 
						TimeZone.getDefault()).getTime());
	}
	
}
