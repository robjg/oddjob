/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;
import org.junit.Before;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.units.DayOfWeek;

/**
 * 
 */
public class DailyScheduleTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger("org.oddjob");
	
   @Before
   public void setUp() {
		logger.debug("============== " + getName() + " ==================");
	}
	
   @Test
	public void testStandardIntervalDifferentStarts() throws ParseException {
		
		DailySchedule test = new DailySchedule();
		test.setFrom("10:00");
		test.setTo("11:00");

		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 09:00"));
		
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-02 10:00"),
				DateHelper.parseDateTime("2006-03-02 11:00")); 
		
		assertEquals(expected, result);
				
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 10:30"));
		
		result = test.nextDue(context);
		
		assertEquals(expected, result);

		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 11:30"));
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-03 10:00"),
				DateHelper.parseDateTime("2006-03-03 11:00"));
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testStandardIntervalRollingNext() throws ParseException {
		
		DailySchedule test = new DailySchedule();
		test.setFrom("10:00");
		test.setTo("11:00");

		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2006-03-02 09:00"));
		
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-02 10:00"),
				DateHelper.parseDateTime("2006-03-02 11:00")); 
		
		assertEquals(expected, result);
				
		context = context.move(result.getToDate());
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-03 10:00"),
				DateHelper.parseDateTime("2006-03-03 11:00"));
		
		assertEquals(expected, result);
		
		context = context.move(result.getToDate());
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-03-04 10:00"),
				DateHelper.parseDateTime("2006-03-04 11:00"));
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testForwardInterval() throws ParseException {
		
		DailySchedule s = new DailySchedule();
		s.setFrom("11:00");
		s.setTo("10:00");

		IntervalTo expected = new IntervalTo(
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

	
   @Test
	public void testSimple() throws ParseException {
		
		DailySchedule s = new DailySchedule();
		s.setFrom("10:00");
		s.setTo("11:00");

		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2005-06-21 9:56");
		context = new ScheduleContext(on);
		
		IntervalTo expected = new IntervalTo( 
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
	
   @Test
	public void testOverMidnight() throws ParseException {
		
		DailySchedule s = new DailySchedule();
		s.setFrom("23:00");
		s.setTo("01:00");

		Date on;
		ScheduleContext context;
		
		// before.
		on = DateHelper.parseDateTime("2005-06-21 02:56");
		context = new ScheduleContext(on);
		
		IntervalTo expected = new IntervalTo(
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
	
   @Test
	public void testOn() throws ParseException {
		
		DailySchedule s = new DailySchedule();
		s.setAt("12:00");

		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2005-06-21 8:00");
		context = new ScheduleContext(on);
		
		IntervalTo expected = new IntervalTo(
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
	
   @Test
	public void testWithLimits() throws ParseException {
		DailySchedule test = new DailySchedule();
		test.setAt("12:00");
		
		Date on;
		ScheduleContext context;
		
		on = DateHelper.parseDateTime("2020-06-21 12:00");
		context = new ScheduleContext(on);
		
		context.spawn(new IntervalTo(
				DateHelper.parseDate("2020-06-21"), 
				DateHelper.parseDate("2020-06-22")));
		
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2020-06-21 12:00"));
		
		assertEquals(expected, result);
	}
	
	// with just a from time
   @Test
	public void testDefaultTo() throws Exception {
		DailySchedule test = new DailySchedule();
		test.setFrom("10:00");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2005-12-25 09:00"));		
		
		Interval result = test.nextDue(context);
		
		logger.debug("result " + result);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2005-12-25 10:00"),
				DateHelper.parseDateTime("2005-12-26 00:00"));
		
		assertEquals(expected, result);
		
		context = context.move(result.getToDate());
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-12-26 10:00"),
				DateHelper.parseDateTime("2005-12-27 00:00"));
	}
	
	// with just a to time
   @Test
	public void testDefaultFrom() throws Exception {
		DailySchedule s = new DailySchedule();
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
	
   @Test
	public void testWithInterval() throws Exception {
		
		DailySchedule test = new DailySchedule();
		test.setFrom("08:00");
		test.setTo("11:59");
		
		IntervalSchedule intervalSchedule = new IntervalSchedule();
		intervalSchedule.setInterval("00:15");
		test.setRefinement(intervalSchedule);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 00:07"));
		
		Interval result = test.nextDue(context);
		
		logger.debug("result " + result);

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 08:00"),
				DateHelper.parseDateTime("2006-02-23 08:15"));
		
		assertEquals(expected, result);
		
		result = test.nextDue(
				context.move(result.getToDate()));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 08:15"),
				DateHelper.parseDateTime("2006-02-23 08:30"));
		
		assertEquals(expected, result);
		
		// In the last interval.
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 11:58"));
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 11:45"),
				DateHelper.parseDateTime("2006-02-23 12:00"));
		
		assertEquals(expected, result);
		
		// Past the to date, but still in the last interval.
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 11:59:05"));
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-23 11:45"),
				DateHelper.parseDateTime("2006-02-23 12:00"));
		
		assertEquals(expected, result);
		
		// past for that day.
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2006-02-23 12:00:00"));
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2006-02-24 08:00"),
				DateHelper.parseDateTime("2006-02-24 08:15"));
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testAsChildWithInterval() throws Exception {
		DailySchedule test = new DailySchedule();
		test.setFrom("10:00");
		test.setTo("17:00");
		
		IntervalSchedule intervalSchedule = new IntervalSchedule();
		intervalSchedule.setInterval("05:00");

		WeeklySchedule dayOfWeek = new WeeklySchedule();
		dayOfWeek.setOn(DayOfWeek.Days.MONDAY);
		
		test.setRefinement(intervalSchedule);
		dayOfWeek.setRefinement(test);
		
		
		ScheduleRoller roller = new ScheduleRoller(dayOfWeek);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2003-05-24 14:00"));
				
		IntervalTo expected;
		
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
	 * So we don't support this!!
	 * 
	 */
   @Test
	public void testTimeAfter24() throws ParseException {
		
		DailySchedule test = new DailySchedule();
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
   @Test
	public void testTwoNestedTimes() throws ParseException {
		
		DailySchedule schedule = new DailySchedule();
		schedule.setFrom("07:00");
		
		DailySchedule retry = new DailySchedule();
		retry.setTo("14:00");
		
		schedule.setRefinement(retry);
		
		ScheduleRoller roller = new ScheduleRoller(schedule);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2009-02-15 13:51"));
		
		assertNull(results[0]);
	}
	
   @Test
	public void testLimitedTimeAndAnInterval() throws ParseException {
		
		DailySchedule retry = new DailySchedule();
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
		
		Interval expected;
		Interval result;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-15 13:45"),
				DateHelper.parseDateTime("2009-02-15 14:00"));
		
		result = retry.nextDue(context);
		
		assertEquals(expected, result);
		
		result = retry.nextDue(context.move(expected.getToDate()));
		
		assertNull(result);
	}
	
   @Test
	public void testDefaultTimesRollingForward() throws ParseException {
		
		DailySchedule test = new DailySchedule();
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2011-06-30 00:00"));
		
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2011-06-30 00:00"),
				DateHelper.parseDateTime("2011-07-01 00:00"));
		
		assertEquals(expected, result);
		
		context = context.move(result.getToDate());
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2011-07-01 00:00"),
				DateHelper.parseDateTime("2011-07-02 00:00"));
		
		assertEquals(expected, result);
	}
}
