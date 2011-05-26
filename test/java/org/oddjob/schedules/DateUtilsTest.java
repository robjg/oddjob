package org.oddjob.schedules;

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;

/**
 * 
 */
public class DateUtilsTest extends TestCase {

	/*
	 * Test for Date startOfDay(Date)
	 */
	public void testStartOfDayDate() throws ParseException {
		assertEquals(
				DateHelper.parseDate("2003-07-11"),
				DateUtils.startOfDay(DateHelper.parseDateTime(
						"2003-07-11 12:27"), TimeZone.getDefault()));
	}

	/*
	 * Test for Date endOfDay(Date)
	 */
	public void testEndOfDayDate() throws ParseException {
		assertEquals(
				DateUtils.oneMillisBefore(DateHelper.parseDate("2003-07-12")),
				DateUtils.endOfDay(DateHelper.parseDateTime(
						"2003-07-11 12:27"), TimeZone.getDefault()));
	}

	/*
	 * Test for int dayOfWeek(Date)
	 */
	public void testDayOfWeekDate() throws ParseException {

		assertEquals(6, DateUtils.dayOfWeek(
			DateHelper.parseDateTime("2003-07-11 12:27"), 
			TimeZone.getDefault()));
	}

	/*
	 * Test for int dayOfMonth(Date)
	 */
	public void testDayOfMonthDate() throws ParseException {

		assertEquals(11, DateUtils.dayOfMonth(
				DateHelper.parseDateTime("2003-07-11 12:27"), 
				TimeZone.getDefault()));
	}


	public void testCompareCalendars() throws ParseException {
		
		Calendar c1 = Calendar.getInstance();
		c1.setTime(DateHelper.parseDateTime("2005-06-21 10:00"));
		Calendar c2 = Calendar.getInstance();
		c2.setTime(DateHelper.parseDateTime("2005-06-21 12:00"));
		
		assertEquals(-1, DateUtils.compare(c1, c2));
		assertEquals(1, DateUtils.compare(c2, c1));
		assertEquals(0, DateUtils.compare(c1, c1));
	}
}
