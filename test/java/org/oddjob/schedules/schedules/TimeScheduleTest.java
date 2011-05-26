/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.DateUtils;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleRoller;

/**
 * 
 */
public class TimeScheduleTest extends TestCase {
	private static final Logger logger = Logger.getLogger("org.oddjob");
	
	protected void setUp() {
		logger.debug("============== " + getName() + " ==================");
	}
	
	public void testStandardInterval() throws ParseException {
		TimeSchedule s = new TimeSchedule();
		s.setFrom("10:00");
		s.setTo("11:00");

		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-02 10:00"),
				DateHelper.parseDateTime("2006-03-02 11:00")); 
		
		Interval result = s.nextDue(new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 09:00")));
		
		assertEquals(expected, result);
				
		result = s.nextDue(new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 10:30")));
		
		assertEquals(expected, result);

		result = s.nextDue(new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 11:30")));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-03 10:00"),
				DateHelper.parseDateTime("2006-03-03 11:00"));
		
		assertEquals(expected, result);
	}
	
	public void testForwardInterval() throws ParseException {
		TimeSchedule s = new TimeSchedule();
		s.setFrom("11:00");
		s.setTo("10:00");

		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-01 11:00"),
				DateHelper.parseDateTime("2006-03-02 10:00"));
		
		Interval result = s.nextDue(new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 09:00")));
		
		assertEquals(expected, result);
		
		result = s.nextDue(new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 10:00")));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-02 11:00"),
				DateHelper.parseDateTime("2006-03-03 10:00"));
		
		assertEquals(expected, result);

		result = s.nextDue(new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 11:30")));
		
		assertEquals(expected, result);
	}

	
	public void testSimple() throws ParseException {
		TimeSchedule s = new TimeSchedule();
		s.setFrom("10:00");
		s.setTo("11:00");

		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2005-06-21 9:56");
		context = new ScheduleContext(on);
		
		Interval expected = new IntervalTo( 
				DateHelper.parseDateTime("2005-06-21 10:00"),
				DateHelper.parseDateTime("2005-06-21 11:00"));

		Interval result = s.nextDue(context);
		
		assertEquals(expected, result);
		
		on = DateHelper.parseDateTime("2005-06-21 10:30");
		context = new ScheduleContext(on);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-06-21 10:00"),
				DateHelper.parseDateTime("2005-06-21 11:00"));

		result = s.nextDue(context);
		
		assertEquals(expected, result);

		on = DateHelper.parseDateTime("2005-06-21 12:30");
		context = new ScheduleContext(on);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-06-22 10:00"),
				DateHelper.parseDateTime("2005-06-22 11:00"));

		result = s.nextDue(context);
		
		assertEquals(expected, result);
	}
	
	public void testOverMidnight() throws ParseException {
		TimeSchedule s = new TimeSchedule();
		s.setFrom("23:00");
		s.setTo("01:00");

		Date on;
		ScheduleContext context;
		
		// before.
		on = DateHelper.parseDateTime("2005-06-21 2:56");
		context = new ScheduleContext(on);
		
		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2005-06-21 23:00"),
				DateHelper.parseDateTime("2005-06-22 01:00"));

		Interval result = s.nextDue(context);
		
		assertEquals(expected, result);

		// during.
		on = DateHelper.parseDateTime("2005-06-22 0:30");
		context = new ScheduleContext(on);

		expected = new IntervalTo (
				DateHelper.parseDateTime("2005-06-21 23:00"),
				DateHelper.parseDateTime("2005-06-22 01:00"));

		result = s.nextDue(context);
		
		assertEquals(expected, result);

		// after
		on = DateHelper.parseDateTime("2005-06-22 01:00");
		context = new ScheduleContext(on);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-06-22 23:00"),
				DateHelper.parseDateTime("2005-06-23 01:00"));

		result = s.nextDue(context);
		
		assertEquals(expected, result);
	}
	
	public void testOn() throws ParseException {
		TimeSchedule s = new TimeSchedule();
		s.setAt("12:00");

		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2005-06-21 8:00");
		context = new ScheduleContext(on);
		
		Interval expected = new IntervalTo(
					DateHelper.parseDateTime("2005-06-21 12:00"));

		Interval result = s.nextDue(context);
		
		assertEquals(expected, result);
		
		on = DateHelper.parseDateTime("2005-06-21 12:00");
		context = new ScheduleContext(on);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-06-21 12:00"));
		
		result = s.nextDue(context);
		
		assertEquals(expected, result);

		on = DateHelper.parseDateTime("2005-06-21 12:30");
		context = new ScheduleContext(on);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-06-22 12:00"));

		result = s.nextDue(context);
		
		assertEquals(expected, result);
	}
	
	/**
	 * How does a Calendar behave at daylight saving time?
	 * change 02:00 to 01:00 and the test fails - but which is right?
	 */
	public void testCalendarAssuptions() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		Date sample = DateHelper.parseDateTime("2005-10-29 02:00");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
		cal.setTime(sample);
		
		cal.add(Calendar.DATE, 1);
		
		assertEquals(DateHelper.parseDateTime("2005-10-30 02:00"), cal.getTime());
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingChangeForward() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule s = new TimeSchedule();
		s.setAt("02:00");

		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2005-10-29 12:00");
		context = new ScheduleContext(on);
		
		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 02:00"));

		Interval result = s.nextDue(context);
		
		assertEquals(expected, result);
		
		on = DateUtils.oneMillisAfter(expected.getToDate());
		context = new ScheduleContext(on);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-31 02:00"));
		
		result = s.nextDue(context);
		
		assertEquals(expected, result);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingChangeBack() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule s = new TimeSchedule();
		s.setAt("02:00");

		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2005-03-26 12:00");
		context = new ScheduleContext(on);
		
		Interval expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"));

		Interval result = s.nextDue(context);
		
		assertEquals(expected, result);
		
		on = DateUtils.oneMillisAfter(result.getToDate());
		context = new ScheduleContext(on);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 02:00"));
		
		result = s.nextDue(context);
		
		assertEquals(expected, result);
		
		TimeZone.setDefault(null);
	}
	
	public void testWithLimits() throws ParseException {
		TimeSchedule s = new TimeSchedule();
		s.setAt("12:00");
		
		Date on;
		ScheduleContext context;
		Date expectedFrom;
		Date expectedTo;
		
		on = DateHelper.parseDateTime("2020-06-21 12:00");
		context = new ScheduleContext(on);
		context.spawn(new IntervalTo(
				DateHelper.parseDate("2020-06-21"), 
				DateHelper.parseDate("2020-06-22")));
		
		expectedFrom = DateHelper.parseDateTime("2020-06-21 12:00");
		expectedTo = expectedFrom;

		Interval result = s.nextDue(context);
		assertEquals(expectedFrom, 
				result.getFromDate());
		assertEquals(expectedTo, 
				result.getToDate());		
	}
	
	// with just a from time
	public void testDefaultTo() throws Exception {
		TimeSchedule s = new TimeSchedule();
		s.setFrom("10:00");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2005-12-25 09:00"));		
		
		Interval result = s.nextDue(context);
		logger.debug("result " + result);
		
		Date expectedFrom = DateHelper.parseDateTime("2005-12-25 10:00");
		Date expectedTo = DateUtils.oneMillisBefore(DateHelper.parseDateTime("2005-12-26 00:00"));
		
		assertEquals(expectedFrom, 
				result.getFromDate());
		assertEquals(expectedTo, 
				result.getToDate());	
	}
	
	// with just a to time
	public void testDefaultFrom() throws Exception {
		TimeSchedule s = new TimeSchedule();
		s.setTo("10:00");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2005-12-25 11:00"));		
		
		Interval result = s.nextDue(context);
		logger.debug("result " + result);
				
		assertEquals(new IntervalTo(		
				DateHelper.parseDateTime("2005-12-26 00:00"),
				DateHelper.parseDateTime("2005-12-26 10:00")),
				result);
	}
	
	public void testWithInterval() throws Exception {
		TimeSchedule timeSchedule = new TimeSchedule();
		timeSchedule.setFrom("08:00");
		timeSchedule.setTo("11:59");
		
		IntervalSchedule intervalSchedule = new IntervalSchedule();
		intervalSchedule.setInterval("00:15");
		timeSchedule.setRefinement(intervalSchedule);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 00:07"));
		
		IntervalTo result = timeSchedule.nextDue(context);
		
		logger.debug("result " + result);

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 08:00"),
				DateHelper.parseDateTime("2006-02-23 08:15"));
		
		assertEquals(expected, result);
		
		result = timeSchedule.nextDue(
				context.move(result.getUpToDate()));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 08:15"),
				DateHelper.parseDateTime("2006-02-23 08:30"));
		
		assertEquals(expected, result);
		
		// In the last interval.
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 11:58"));
		
		result = timeSchedule.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 11:45"),
				DateHelper.parseDateTime("2006-02-23 12:00"));
		
		assertEquals(expected, result);
		
		// Past the to date, but still in the last interval.
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 11:59:05"));
		
		result = timeSchedule.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 11:45"),
				DateHelper.parseDateTime("2006-02-23 12:00"));
		
		assertEquals(expected, result);
		
		// past for that day.
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 12:00:00"));
		
		result = timeSchedule.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-24 08:00"),
				DateHelper.parseDateTime("2006-02-24 08:15"));
		
		assertEquals(expected, result);
	}
	
	public void testAsChildWithInterval() throws Exception {
		TimeSchedule test = new TimeSchedule();
		test.setFrom("10:00");
		test.setTo("17:00");
		
		IntervalSchedule intervalSchedule = new IntervalSchedule();
		intervalSchedule.setInterval("05:00");

		DayOfWeekSchedule dayOfWeek = new DayOfWeekSchedule();
		dayOfWeek.setOn(1);
		
		test.setRefinement(intervalSchedule);
		dayOfWeek.setRefinement(test);
		
		
		ScheduleRoller roller = new ScheduleRoller(dayOfWeek);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2003-05-24 14:00"));
				
		Interval expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2003-05-26 10:00"),
				DateHelper.parseDateTime("2003-05-26 15:00"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2003-05-26 15:00"),
				DateHelper.parseDateTime("2003-05-26 20:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2003-06-02 10:00"),
				DateHelper.parseDateTime("2003-06-02 15:00"));
		
		assertEquals(expected, results[2]);
		
	}

	/**
	 * Weird things happen with more than 24 hours in a day...
	 */
	public void testTimeAfter24() throws ParseException {
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("10:00");
		test.setTo("25:00");
		
		ScheduleContext context; 
		Interval expected;
		Interval result;
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-27"));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-26 10:00"),
				DateHelper.parseDateTime("2009-02-28 01:00"));
		
		result = test.nextDue(context);
		
		assertEquals(expected, result);
		
		context = new ScheduleContext(
				DateHelper.parseDate("2009-02-27 12:00"));

		result = test.nextDue(context);
		
		assertEquals(expected, result);
				
	}

	/**
	 * Note that the retry is never due because it starts before the
	 * schedule, and so is limited by it to being never due.
	 * 
	 * @throws ParseException
	 */
	public void testTwoNestedTimes() throws ParseException {
		
		TimeSchedule schedule = new TimeSchedule();
		schedule.setFrom("07:00");
		
		TimeSchedule retry = new TimeSchedule();
		retry.setTo("14:00");
		
		schedule.setRefinement(retry);
		
		ScheduleRoller roller = new ScheduleRoller(schedule);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2009-02-15 13:51"));
		
		assertNull(results[0]);
	}
	
	public void testLimitedTimeAndAnInterval() throws ParseException {
		
		TimeSchedule retry = new TimeSchedule();
		retry.setFrom("07:00");
		retry.setTo("14:00");
		
		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("00:15");
		
		retry.setRefinement(interval);

		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-15 13:51"));
				
		context = context.spawn(
				new IntervalTo(
						DateHelper.parseDateTime("2009-02-15 07:00"),
						DateHelper.parseDateTime("2009-02-16 00:00")));
		
		IntervalTo expected;
		Interval result;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-15 13:45"),
				DateHelper.parseDateTime("2009-02-15 14:00"));
		
		result = retry.nextDue(context);
		
		assertEquals(expected, result);
		
		result = retry.nextDue(context.move(expected.getUpToDate()));
		
		assertNull(result);
	}
}
