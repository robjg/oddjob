package org.oddjob.schedules;

import java.text.ParseException;

import junit.framework.TestCase;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.schedules.DayOfWeekSchedule;

public class ConstrainedScheduleTest extends TestCase {

	public void testLastDayOfWeek() throws ParseException {
		
		DayOfWeekSchedule test = new DayOfWeekSchedule();
		test.setFrom(1);
		test.setTo(5);
		
		Interval expected = new IntervalTo(
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
	
	public void testLastDayOfWeekOverllaping() throws ParseException {
		
		DayOfWeekSchedule test = new DayOfWeekSchedule();
		test.setFrom(5);
		test.setTo(1);
		
		Interval expected = new IntervalTo(
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
	
	public void testNextDayOfWeek() throws ParseException {
		
		DayOfWeekSchedule test = new DayOfWeekSchedule();
		test.setFrom(1);
		test.setTo(5);
		
		Interval expected = new IntervalTo(
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
	
	public void testNextDayOfWeekOverllaping() throws ParseException {
		
		DayOfWeekSchedule test = new DayOfWeekSchedule();
		test.setFrom(5);
		test.setTo(1);
		
		Interval expected = new IntervalTo(
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
