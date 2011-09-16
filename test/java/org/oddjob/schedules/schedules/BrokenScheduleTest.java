/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleList;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;

/**
 * 
 */
public class BrokenScheduleTest extends TestCase {
	
	static Schedule brokenSchedule() throws ParseException, NoConversionAvailableException, ConversionFailedException {
		
		WeeklySchedule d1 = new WeeklySchedule();
		d1.setOn(DayOfWeek.Days.MONDAY);
		WeeklySchedule d2 = new WeeklySchedule();
		d2.setOn(DayOfWeek.Days.WEDNESDAY);

		ScheduleList s1 = new ScheduleList();
		s1.setSchedules(new Schedule[] { d1, d2 });
		
		YearlySchedule s2 = new YearlySchedule();
		s2.setFromDate("02-15");
		s2.setToDate("02-25");
		
		BrokenSchedule b = new BrokenSchedule();
		b.setSchedule(s1);
		b.setBreaks(s2);
		
		return b;
	}
	
	public void testNextDue1() throws Exception {
		Schedule s = brokenSchedule();
		
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(s);
		
		Interval[] results = roller.resultsFrom(DateHelper.parseDate("2003-02-01"));

		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-03"),
				DateHelper.parseDate("2003-02-04"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-06"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-10"),
				DateHelper.parseDate("2003-02-11"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-12"),
				DateHelper.parseDate("2003-02-13"));
		
		assertEquals(expected, results[3]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-26"),
				DateHelper.parseDate("2003-02-27"));
		
		assertEquals(expected, results[4]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-03-03"),
				DateHelper.parseDate("2003-03-04"));
		
		assertEquals(expected, results[5]);

	}
	
	public void testNextDue2() throws Exception {
		Schedule s = brokenSchedule();
				
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(s);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2003-02-15"));

		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-26"),
				DateHelper.parseDate("2003-02-27"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-03-03"),
				DateHelper.parseDate("2003-03-04"));
		
		assertEquals(expected, results[1]);

	}

	public void testNextDue3() throws Exception {
		Schedule s = brokenSchedule();
		
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(s);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2003-02-26"));

		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-26"),
				DateHelper.parseDate("2003-02-27"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-03-03"),
				DateHelper.parseDate("2003-03-04"));
		
		assertEquals(expected, results[1]);

	}
	
	/** 
	 * If a schedule spans the break, the break is ignored.
	 * (because the schedule start before the break).
	 * 
	 * @throws ParseException
	 */
	public void testScheduleSpansBreaks() throws ParseException {
		
		MonthlySchedule schedule = new MonthlySchedule();
		
		MonthlySchedule breaks = new MonthlySchedule();
		breaks.setOnDay(new DayOfMonth.Number(13));
		
		BrokenSchedule test = new BrokenSchedule();
		test.setBreaks(breaks);
		test.setSchedule(schedule);
		
		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2011-09-10"));
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2011-09-01"),
				DateHelper.parseDate("2011-10-01"));
		
		assertEquals(expected, results[0]);
		
	}
	
	/**
	 * Test when a time overlaps into the break.
	 * 
	 * @throws ParseException
	 */
	public void testOverlappingSchedule() throws ParseException {
		
		WeeklySchedule dayOfWeekSchedule = new WeeklySchedule();
		dayOfWeekSchedule.setOn(DayOfWeek.Days.THURSDAY);
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("22:00");
		time.setTo("02:00");

		dayOfWeekSchedule.setRefinement(time);
		
		DateSchedule aBreak = new DateSchedule();
		aBreak.setFrom("2009-02-27"); // A Friday
		aBreak.setTo("2009-03-09"); // A week Monday

		BrokenSchedule test = new BrokenSchedule();
		
		test.setSchedule(dayOfWeekSchedule);
		test.setBreaks(aBreak);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-26 22:00"),
				DateHelper.parseDateTime("2009-02-27 02:00"));
		
		ScheduleContext scheduleContext = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-24 12:00"));
		
		ScheduleResult result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-02-27 01:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-03-12 22:00"),
				DateHelper.parseDateTime("2009-03-13 02:00"));
		
		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-02-30 12:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-03-03 12:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-03-11 00:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);
	}
	
	/**
	 * The broken schedule doesn't let the interval schedules finish.
	 * This probably isn't what we want.
	 * 
	 * @throws ParseException
	 */
	public void testOverlappingIntervalSchedule() throws ParseException {
		
		WeeklySchedule dayOfWeekSchedule = new WeeklySchedule();
		dayOfWeekSchedule.setOn(DayOfWeek.Days.THURSDAY);
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("22:00");
		time.setTo("02:00");

		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("01:00");
		
		dayOfWeekSchedule.setRefinement(time);
		time.setRefinement(interval);
		
		DateSchedule aBreak = new DateSchedule();
		aBreak.setFrom("2009-02-27");
		aBreak.setTo("2009-03-09");

		BrokenSchedule test = new BrokenSchedule();
		
		test.setSchedule(dayOfWeekSchedule);
		test.setBreaks(aBreak);
		
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2009-02-26 23:30"));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-26 23:00"),
				DateHelper.parseDateTime("2009-02-27 00:00"));
		
		assertEquals(expected, results[0]);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-03-12 22:00"),
				DateHelper.parseDateTime("2009-03-12 23:00"));
		
		assertEquals(expected, results[1]);
	}
	
    public void testBrokenScheduleExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/BrokenScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleResult next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-12-24 11:00")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-12-27 10:00"));
    	
    	assertEquals(expected, next);
    }
}
