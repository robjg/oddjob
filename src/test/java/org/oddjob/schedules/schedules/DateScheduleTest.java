/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

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

/**
 * 
 */
public class DateScheduleTest extends OjTestCase {

   @Test
	public void testAllOfTime() throws ParseException {
		
		DateSchedule test = new DateSchedule();
		
		ScheduleContext scheduleContext = new ScheduleContext(new Date());

		IntervalTo expected = new IntervalTo(
				Interval.START_OF_TIME,
				Interval.END_OF_TIME);
		
		Interval result = test.nextDue(scheduleContext);
				
		assertEquals(expected, result);
	}
	
   @Test
	public void testNextDueSingleDayDate() throws ParseException {
		DateSchedule test = new DateSchedule();
		test.setFrom("2003-02-05");
		test.setTo("2003-02-25");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDate("2003-02-02"));
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-26"));
		
		assertEquals(expected, result);
		
		context = context.move(result.getToDate());
		
		result = test.nextDue(context);
		
		assertNull(result);
	}

   @Test
	public void testNextDueDateRange() throws ParseException {
		DateSchedule test = new DateSchedule();
		test.setFrom("2003-02-05");
		test.setTo("2003-02-25");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDate("2003-02-15"));
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-26"));
		
		assertEquals(expected, result);
		
		context = context.move(result.getToDate());
		
		result = test.nextDue(context);
		
		assertEquals(null, result);
	}

   @Test
	public void testNextDueAfterDate() throws ParseException {
		DateSchedule test = new DateSchedule();
		test.setFrom("2003-02-05");
		test.setTo("2003-02-25");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDate("2003-02-27"));
		Interval result = test.nextDue(context);
		
		assertNull(result);
	}
	
   @Test
	public void testDueOnWithTimeRefinement() throws ParseException {
		
		// set schedule
		DateSchedule test = new DateSchedule();
		test.setOn("2003-12-25");
		
		DailySchedule timeSchedule = new DailySchedule();
		timeSchedule.setAt("12:00");
		
		test.setRefinement(timeSchedule);
	
    	ScheduleContext context = 
    		new ScheduleContext(DateHelper.parseDateTime("2003-02-16"));
    	
    	Interval result = test.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2003-12-25 12:00"));
    	
    	assertEquals(expected, result); 

    	context = context.move(result.getToDate());
    	
    	result = test.nextDue(context);

    	assertNull(result);
	}
	
   @Test
    public void testDateScheduleExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DateScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2004-12-24 11:00")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2004-12-25 00:00"),
    			DateHelper.parseDateTime("2004-12-26 00:00"));
    	
    	assertEquals(expected, next);

    	next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2004-12-26 11:00")));
    	
    	assertEquals(null, next);
    }
}
