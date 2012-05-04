/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.units.DayOfWeek;

public class IntervalScheduleTest extends TestCase {

	public void testSimple() throws ParseException {
		
		IntervalSchedule test = new IntervalSchedule();
		test.setInterval("00:00:05");

		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-11-25 10:00"));
		
		IntervalTo expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-25 10:00:00"),
				DateHelper.parseDateTime("2005-11-25 10:00:05"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-25 10:00:05"),
				DateHelper.parseDateTime("2005-11-25 10:00:10"));
		
		assertEquals(expected, results[1]);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-25 10:00:10"),
				DateHelper.parseDateTime("2005-11-25 10:00:15"));
		
		assertEquals(expected, results[2]);
	}
	
	public void testSimpleConstrained() throws ParseException {
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("09:55");
		
		IntervalSchedule test = new IntervalSchedule();
		test.setInterval("00:00:05");

		time.setRefinement(test);
		
		ScheduleRoller roller = new ScheduleRoller(time);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-11-25 10:00"));
		
		IntervalTo expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-25 10:00:00"),
				DateHelper.parseDateTime("2005-11-25 10:00:05"));
		
		assertEquals(expected, results[0]);
		
		// the same even in the middle
		
		results = roller.resultsFrom(
				DateHelper.parseDateTime("2005-11-25 10:00:04"));
				
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-25 10:00:05"),
				DateHelper.parseDateTime("2005-11-25 10:00:10"));
		
		assertEquals(expected, results[1]);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2005-11-25 10:00:10"),
				DateHelper.parseDateTime("2005-11-25 10:00:15"));
		
		assertEquals(expected, results[2]);
	}
	
	/**
	 * An interval that extends past the parent interval.
	 * 
	 * @throws ParseException
	 */
	public void testEvery7HoursOnWednesday() throws ParseException {
		
		WeeklySchedule schedule = new WeeklySchedule();
		schedule.setOn(DayOfWeek.Days.WEDNESDAY);
		
		IntervalSchedule test = new IntervalSchedule();
		
		test.setInterval("7:00");
		schedule.setRefinement(test);

		ScheduleRoller roller = new ScheduleRoller(schedule);

		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2009-02-18 10:00"));
		
		IntervalTo expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-18 07:00"),
				DateHelper.parseDateTime("2009-02-18 14:00"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-18 14:00"),
				DateHelper.parseDateTime("2009-02-18 21:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-18 21:00"),
				DateHelper.parseDateTime("2009-02-19 04:00"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-25 00:00"),
				DateHelper.parseDateTime("2009-02-25 07:00"));
		
		assertEquals(expected, results[3]);
	}
	
	/**
	 * An interval that extends past the parent interval
	 * and now is in the extra bit.
	 * 
	 * @throws ParseException
	 */
	public void testEvery7HoursOnWednesday2() throws ParseException {
		
		WeeklySchedule schedule = new WeeklySchedule();
		schedule.setOn(DayOfWeek.Days.WEDNESDAY);
		
		IntervalSchedule test = new IntervalSchedule();
		
		test.setInterval("7:00");
		schedule.setRefinement(test);

		ScheduleRoller roller = new ScheduleRoller(schedule);

		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2009-02-19 03:00"));
		
		IntervalTo expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-18 21:00"),
				DateHelper.parseDateTime("2009-02-19 04:00"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-25 00:00"),
				DateHelper.parseDateTime("2009-02-25 07:00"));
		
		assertEquals(expected, results[1]);
	}
	
	public void testOverMidnightWednesday() throws ParseException {
		
		WeeklySchedule day = new WeeklySchedule();
		day.setOn(DayOfWeek.Days.WEDNESDAY);

		DailySchedule time = new DailySchedule();
		time.setFrom("23:00");
		time.setTo("02:00");
		
		IntervalSchedule test = new IntervalSchedule();		
		test.setInterval("1:00");
		
		day.setRefinement(time);
		time.setRefinement(test);
	
		ScheduleContext scheduleContext = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-18"));
		
		Interval nextDue;
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-18 23:00"),
				DateHelper.parseDateTime("2009-02-19 00:00"));
		
		nextDue = day.nextDue(scheduleContext);
		
		assertEquals(expected, nextDue);
		
		scheduleContext = scheduleContext.move(expected.getToDate());
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-19 00:00"),
				DateHelper.parseDateTime("2009-02-19 01:00"));
		
		nextDue = day.nextDue(scheduleContext);
		
		assertEquals(expected, nextDue);
		
		scheduleContext = scheduleContext.move(expected.getToDate());
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-19 01:00"),
				DateHelper.parseDateTime("2009-02-19 02:00"));
		
		nextDue = day.nextDue(scheduleContext);
		
		assertEquals(expected, nextDue);
		
	}
	
	public void testInALargeParentInterval() throws ParseException {
	
		DateSchedule date = new DateSchedule();
		date.setFrom("2000-01-01");
		
		IntervalSchedule test = new IntervalSchedule();
		test.setInterval("00:00:05");
			
		date.setRefinement(test);

		ScheduleContext scheduleContext = new ScheduleContext(
				DateHelper.parseDateTime("2009-03-03 11:17:04:999"));
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2009-03-03 11:17:00"),
				DateHelper.parseDateTime("2009-03-03 11:17:05"));
			
		Interval result = date.nextDue(scheduleContext);
		
		assertEquals(expected, result);
	}
	
    public void testIntervalExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/IntervalExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2010-02-15 11:00")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2010-02-15 11:00"),
    			DateHelper.parseDateTime("2010-02-15 11:20"));
    	
    	assertEquals(expected, next);
    }
    
    public void testBadInterval() throws ParseException {
    	
		IntervalSchedule test = new IntervalSchedule();		
		test.setInterval("00:00.500");
		
		ScheduleContext scheduleContext = new ScheduleContext(
				DateHelper.parseDateTime("2012-05-01 12:00"));
		
		try {
			test.nextDue(scheduleContext);
			fail("Should fail.");
		}
		catch (IllegalStateException e) {
			//
		}
		
    }
}
