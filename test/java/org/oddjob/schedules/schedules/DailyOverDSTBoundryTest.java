package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.utils.SpringSafeCalendar;
import org.oddjob.arooa.utils.TimeParser;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;

public class DailyOverDSTBoundryTest extends TestCase {

	private static final Logger logger = Logger.getLogger(DailyOverDSTBoundryTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("--------------------  " + getName() + "  ----------------------");
	}
	
	/**
	 * How does a Calendar behave at daylight saving time?
	 * <p>
	 * When the clocks go back in autumn there is an extra hour. In
	 * the UK this is 01:00 to 02:00 BST. At 02:00 BST the time becomes 01:00 
	 * GMT.
	 * <p>
	 * The time format will parse 01:00 as 01:00 GMT. A daily schedule
	 * running from before the Sunday will be due at 01:00 BST Sunday, 
	 * 24 hours after it last ran. But will then be due at 01:00 GMT Monday, 
	 * 25 hours later.
	 * <p>The same daily schedule started after Midnight on the Sunday will 
	 * be due at 01:00 GMT Sunday and then 01:00 GMT Monday, only 24 hours later.
	 * 
	 */
	public void testCalendarAssuptionsAutumn() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		// Autumn
		
		Date saturday1AM_BST = DateHelper.parseDateTime("2005-10-29 01:00");
				
		Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
		cal1.setTime(saturday1AM_BST);
		
		cal1.add(Calendar.DATE, 1);
		
		assertEquals(1 * 60 * 60 * 1000L, cal1.get(Calendar.DST_OFFSET));
		
		Date sunday1AM_BST = new Date(DateHelper.parseDateTime("2005-10-30 00:59:59.999").getTime() + 1);
		
		logger.info("Sunday 1am BST: " + saturday1AM_BST);
		
		assertEquals(sunday1AM_BST, cal1.getTime());
		
		cal1.add(Calendar.HOUR, 1);

		Date sunday1AM_GMT = DateHelper.parseDateTime("2005-10-30 01:00");
		
		logger.info("Sunday 1am GMT: " + sunday1AM_GMT);
		
		assertEquals(sunday1AM_GMT, cal1.getTime());
		
		TimeZone.setDefault(null);
	}
	
	/**
	 * Adding a day to 1am Saturday is midnight Sunday (23 hours later)
	 * 
	 * @throws ParseException
	 */
	public void testCalendarAssuptionsSpring() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
			
		// Spring
		
		// First with midnight
		Date saturday_Midnight = DateHelper.parseDateTime("2005-03-26 00:00");		

		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(saturday_Midnight);

		cal1.add(Calendar.DATE, 1);
		
		Date sundayMidnight_GMT = DateHelper.parseDateTime("2005-03-27 00:00"); 
		
		assertEquals(sundayMidnight_GMT, cal1.getTime());
		
		// Then from 1 am - same outcome!
		Date saturday1AM_GMT = DateHelper.parseDateTime("2005-03-26 01:00");		
						
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(saturday1AM_GMT);
		
		cal2.add(Calendar.DATE, 1);
				
		assertEquals(sundayMidnight_GMT, cal2.getTime());
		
		// Check it's 23 hours.
		assertEquals(23 * 60 * 60 * 1000L, sundayMidnight_GMT.getTime() - saturday1AM_GMT.getTime());
		
		cal2.add(Calendar.HOUR, 1);
				
		Date sunday_2AM_BST = DateHelper.parseDateTime("2005-03-27 02:00"); 

		logger.info("Sunday 2am BST: " + sunday_2AM_BST);
		
		assertEquals(sunday_2AM_BST, cal2.getTime());
						
		TimeZone.setDefault(null);
	}
	
	public void testDateParsing() throws ParseException {
		
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		Calendar cal = Calendar.getInstance();
		
		// What happens to 1am in Spring? It is 2am BST.
		
		Date oneAM = DateHelper.parseDateTime("2005-03-27 01:05"); 

		logger.info("2005-03-27 01:05 is " + oneAM); 
		
		cal.setTime(oneAM);
		
		assertEquals(1 * 60 * 60 * 1000L, cal.get(Calendar.DST_OFFSET));
		assertEquals(2, cal.get(Calendar.HOUR));
		
		Date twoAM = DateHelper.parseDateTime("2005-03-27 02:05"); 

		cal.setTime(twoAM);
		
		assertEquals(1 * 60 * 60 * 1000L, cal.get(Calendar.DST_OFFSET));
		assertEquals(2, cal.get(Calendar.HOUR));
		
		assertEquals(oneAM, twoAM);
		
		Date midnightGMT = DateHelper.parseDateTime("2005-03-27 00:55"); 

		logger.info("2005-03-27 00:55 is " + midnightGMT); 
				
		cal.setTime(midnightGMT);
		
		assertEquals(0L, cal.get(Calendar.DST_OFFSET));
		assertEquals(0, cal.get(Calendar.HOUR));
		
		// Time between?
		
		long interval = DateHelper.parseDateTime("2005-03-27 01:05").getTime() - 
			DateHelper.parseDateTime("2005-03-27 00:55").getTime();
		
		assertEquals(10 * 60 * 1000L, interval);
		
		interval = DateHelper.parseDateTime("2005-03-27 02:05").getTime() - 
		DateHelper.parseDateTime("2005-03-27 01:55").getTime();
		
		assertEquals(-50 * 60 * 1000L, interval);
		
		interval = DateHelper.parseDateTime("2005-03-27 02:05").getTime() - 
		DateHelper.parseDateTime("2005-03-27 00:55").getTime();
		
		assertEquals(10 * 60 * 1000L, interval);
	}
	
	//
	// At boundary start.
	
	public void testDayLightSavingInAutumnWithAtBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setAt("01:00");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-29 01:00"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:00"));

		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-31 01:00"));
		
		assertEquals(expected, results[2]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithAtBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setAt("01:00");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-26 01:00"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 01:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 01:00"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-29 01:00"));
		
		assertEquals(expected, results[3]);
		
		TimeZone.setDefault(null);
	}

	//
	// At boundary end.
	
	public void testDayLightSavingInAutumnWithAtBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setAt("02:00");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-29 02:00"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 02:00"));

		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-31 02:00"));
		
		assertEquals(expected, results[2]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithAtBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setAt("02:00");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-26 02:00"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 02:00"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-29 02:00"));
		
		assertEquals(expected, results[3]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// On boundary.
	
	public void testDayLightSavingInAutumnWithFromToOnBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("01:00");
		test.setTo("02:00");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-29 01:00"),
				DateHelper.parseDateTime("2005-10-29 02:00"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:00"),
				DateHelper.parseDateTime("2005-10-30 02:00"));

		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-31 01:00"),
				DateHelper.parseDateTime("2005-10-31 02:00"));
		
		assertEquals(expected, results[2]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithFromToOnBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("01:00");
		test.setTo("02:00");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-26 01:00"),
				DateHelper.parseDateTime("2005-03-26 02:00"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 01:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 01:00"),
				DateHelper.parseDateTime("2005-03-28 02:00"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-29 01:00"),
				DateHelper.parseDateTime("2005-03-29 02:00"));
		
		assertEquals(expected, results[3]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// Spanning boundary start
	
	public void testDayLightSavingInAutumnWithFromToSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("00:45");
		test.setTo("01:15");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-29 00:45"),
				DateHelper.parseDateTime("2005-10-29 01:15"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 00:45"),
				DateHelper.parseDateTime("2005-10-30 01:15"));

		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-31 00:45"),
				DateHelper.parseDateTime("2005-10-31 01:15"));
		
		assertEquals(expected, results[2]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringWithFromToSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("00:45");
		test.setTo("01:15");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-26 00:45"),
				DateHelper.parseDateTime("2005-03-26 01:15"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 00:45"),
				DateHelper.parseDateTime("2005-03-27 02:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 00:45"),
				DateHelper.parseDateTime("2005-03-28 01:15"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-29 00:45"),
				DateHelper.parseDateTime("2005-03-29 01:15"));
		
		assertEquals(expected, results[3]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// Spanning boundary end
	
	public void testDayLightSavingInAutumnWithFromToSpanningBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("01:45");
		test.setTo("02:15");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-29 01:45"),
				DateHelper.parseDateTime("2005-10-29 02:15"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:45"),
				DateHelper.parseDateTime("2005-10-30 02:15"));

		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-31 01:45"),
				DateHelper.parseDateTime("2005-10-31 02:15"));
		
		assertEquals(expected, results[2]);
		
		TimeZone.setDefault(null);
	}
	
	/**
	 * This is the test that proves the need for {@link TimeParser} with {@link SpringSafeCalendar}. 
	 * 
	 * @throws ParseException
	 */
	public void testDayLightSavingInSpringWithFromToSpanningBoundry2() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("01:45");
		test.setTo("02:15");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-26 01:45"),
				DateHelper.parseDateTime("2005-03-26 02:15"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"),
				DateHelper.parseDateTime("2005-03-27 02:15"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 01:45"),
				DateHelper.parseDateTime("2005-03-28 02:15"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-29 01:45"),
				DateHelper.parseDateTime("2005-03-29 02:15"));
		
		assertEquals(expected, results[3]);
		
		TimeZone.setDefault(null);
	}
	
	//
	// Over Midnight (from > to)
	
	public void testDayLightSavingInAutumnOverMidnightSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("01:30");
		test.setTo("00:30");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-10-28 12:00"));
		
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-28 01:30"),
				DateHelper.parseDateTime("2005-10-29 00:30"));

		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-29 01:30"),
				DateHelper.parseDateTime("2005-10-30 00:30"));

		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-10-30 01:30"),
				DateHelper.parseDateTime("2005-10-31 00:30"));
		
		assertEquals(expected, results[2]);
		
		TimeZone.setDefault(null);
	}
	
	public void testDayLightSavingInSpringOverMidnightSpanningBoundry() throws ParseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		
		DailySchedule test = new DailySchedule();
		test.setFrom("01:30");
		test.setTo("00:30");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		ScheduleResult[] results = roller.resultsFrom(
				DateHelper.parseDate("2005-03-26 00:00"));
				
		ScheduleResult expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-25 01:30"),
				DateHelper.parseDateTime("2005-03-26 00:30"));

		assertEquals(expected, results[0]);
				
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-26 01:30"),
				DateHelper.parseDateTime("2005-03-27 00:30"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-27 02:00"),
				DateHelper.parseDateTime("2005-03-28 00:30"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-03-28 01:30"),
				DateHelper.parseDateTime("2005-03-29 00:30"));
		
		assertEquals(expected, results[3]);
		
		TimeZone.setDefault(null);
	}
}
