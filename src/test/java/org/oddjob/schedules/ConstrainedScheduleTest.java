package org.oddjob.schedules;

import org.junit.Test;

import java.text.ParseException;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.schedules.units.DayOfWeek;

public class ConstrainedScheduleTest extends OjTestCase {

   @Test
	public void testLastDayOfWeek() throws ParseException {
		
		WeeklySchedule test = new WeeklySchedule();
		test.setFrom(DayOfWeek.Days.MONDAY);
		test.setTo(DayOfWeek.Days.FRIDAY);
		
		IntervalBase expected = new IntervalTo(
        		DateHelper.parseDateTime("2009-02-16 00:00"), 
        		DateHelper.parseDateTime("2009-02-21 00:00"));
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-21 00:00"));
    	
		Interval result = test.lastInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-02-23"));
		
		result = test.lastInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-02-27 23:00"));
		
		result = test.lastInterval(context);
		
		assertEquals(expected, result);
		
	}
	
   @Test
	public void testLastDayOfWeekOverllaping() throws ParseException {
		
		WeeklySchedule test = new WeeklySchedule();
		test.setFrom(DayOfWeek.Days.FRIDAY);
		test.setTo(DayOfWeek.Days.MONDAY);
		
		IntervalBase expected = new IntervalTo(
        		DateHelper.parseDateTime("2009-02-27 00:00"), 
        		DateHelper.parseDateTime("2009-03-03 00:00"));
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-03-03"));
    	
		Interval result = test.lastInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-03-07"));
		
		result = test.lastInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-03-09 23:00"));
		
		result = test.lastInterval(context);
		
		assertEquals(expected, result);
		
	}
	
   @Test
	public void testNextDayOfWeek() throws ParseException {
		
		WeeklySchedule test = new WeeklySchedule();
		test.setFrom(DayOfWeek.Days.MONDAY);
		test.setTo(DayOfWeek.Days.FRIDAY);
		
		IntervalBase expected = new IntervalTo(
        		DateHelper.parseDateTime("2009-02-23 00:00"), 
        		DateHelper.parseDateTime("2009-02-28 00:00"));
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-21 00:00"));
    	
		Interval result = test.nextInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-02-23"));
		
		result = test.nextInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-02-27 23:00"));
		
		result = test.nextInterval(context);
		
		assertEquals(expected, result);
		
	}
	
   @Test
	public void testNextDayOfWeekOverllaping() throws ParseException {
		
		WeeklySchedule test = new WeeklySchedule();
		test.setFrom(DayOfWeek.Days.FRIDAY);
		test.setTo(DayOfWeek.Days.MONDAY);
		
		IntervalBase expected = new IntervalTo(
        		DateHelper.parseDateTime("2009-03-06 00:00"), 
        		DateHelper.parseDateTime("2009-03-10 00:00"));
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-03-03"));
    	
		Interval result = test.nextInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-03-07"));
		
		result = test.nextInterval(context);
		
		assertEquals(expected, result);
		
		context = context.move(
				DateHelper.parseDateTime("2009-03-09 23:00"));
		
		result = test.nextInterval(context);
		
		assertEquals(expected, result);
		
	}
	
}
