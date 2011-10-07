package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.units.DayOfWeek;

public class TimeOverDSTBoundryTest extends TestCase {

	//
	// At boundary start.
	
	public void testDayLightSavingInAutumnWithAtBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setAt("01:00");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:00"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-06 01:00"));

		assertEquals(expected, results[1]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithAtBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setAt("01:00");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected; 
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 01:00"));
		

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-04-03 01:00"));
		
		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}

	//
	// At boundary end.

	public void testDayLightSavingInAutumnWithAtBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setAt("02:00");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 02:00"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-06 02:00"));

		assertEquals(expected, results[1]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithAtBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setAt("02:00");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"));
		
		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-04-03 02:00"));
		
		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}
	
	//
	// On boundary.
	
	public void testDayLightSavingInAutumnWithFromToOnBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("01:00");
		test.setTo("02:00");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:00"),
				DateHelper.parseDateTime("2005-10-30 02:00"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-06 01:00"),
				DateHelper.parseDateTime("2005-11-06 02:00"));

		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithFromToOnBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("01:00");
		test.setTo("02:00");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 01:00"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-04-03 01:00"),
				DateHelper.parseDateTime("2005-04-03 02:00"));
		
		assertEquals(expected, results[1]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// Spanning boundary start
	
	public void testDayLightSavingInAutumnWithFromToSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("00:45");
		test.setTo("01:15");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 00:45"),
				DateHelper.parseDateTime("2005-10-30 01:15"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-06 00:45"),
				DateHelper.parseDateTime("2005-11-06 01:15"));

		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithFromToSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("00:45");
		test.setTo("01:15");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 00:45"),
				DateHelper.parseDateTime("2005-03-27 02:00"));
		
		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-04-03 00:45"),
				DateHelper.parseDateTime("2005-04-03 01:15"));
		
		assertEquals(expected, results[1]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// Spanning boundary end
	
	public void testDayLightSavingInAutumnWithFromToSpanningBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("01:45");
		test.setTo("02:15");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:45"),
				DateHelper.parseDateTime("2005-10-30 02:15"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-06 01:45"),
				DateHelper.parseDateTime("2005-11-06 02:15"));

		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithFromToSpanningBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("01:45");
		test.setTo("02:15");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"),
				DateHelper.parseDateTime("2005-03-27 02:15"));
		
		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-04-03 01:45"),
				DateHelper.parseDateTime("2005-04-03 02:15"));
		
		assertEquals(expected, results[1]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// Over Midnight (from > to)

	public void testDayLightSavingInAutumnOverMidnightSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("01:30");
		test.setTo("00:30");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:30"),
				DateHelper.parseDateTime("2005-10-31 00:30"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-06 01:30"),
				DateHelper.parseDateTime("2005-11-07 00:30"));

		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringOverMidnightSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		TimeSchedule test = new TimeSchedule();
		test.setFrom("01:30");
		test.setTo("00:30");

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.SUNDAY);

		weekly.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(weekly);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"),
				DateHelper.parseDateTime("2005-03-28 00:30"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-04-03 01:30"),
				DateHelper.parseDateTime("2005-04-04 00:30"));
		
		assertEquals(expected, results[1]);
				
		TimeZone.setDefault(null);
	}
}
