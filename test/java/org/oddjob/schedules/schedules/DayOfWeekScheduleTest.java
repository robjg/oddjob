/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;

/**
 *
 * @author Rob Gordon.
 */
public class DayOfWeekScheduleTest extends TestCase {
    
    public void testFromAndTo() throws ParseException {
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setFrom(2);
        schedule.setTo(3);
 
        // 10th Feb 2004 was a Tuesday
        Date now1 = DateHelper.parseDateTime("2004-02-10 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2004-02-10"),
        		DateHelper.parseDateTime("2004-02-12"));

        IntervalTo result = schedule.nextDue(
        		new ScheduleContext(now1));
        
        assertEquals(expected, result);
    }
    
    public void testAfter() throws ParseException {
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setFrom(2);
        schedule.setTo(3);
        
        Date now1 = DateHelper.parseDateTime("2004-02-14 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2004-02-17"),
        		DateHelper.parseDateTime("2004-02-19"));
        
        IntervalTo result = schedule.nextDue(new ScheduleContext(now1));
        
        assertEquals(expected, result);
    }
    
    public void testOverBoundry() throws ParseException {
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setFrom(5);
        schedule.setTo(1);
        
        Date now1 = DateHelper.parseDateTime("2004-02-11 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2004-02-13"),
        		DateHelper.parseDateTime("2004-02-17"));
        		
        IntervalTo result1 = schedule.nextDue(
        		new ScheduleContext(now1));
        
        assertEquals(expected, result1);

        Date now2 = DateHelper.parseDateTime("2004-02-14 12:30");
        
        IntervalTo result2 = schedule.nextDue(
        		new ScheduleContext(now2));
        
        assertEquals(expected, result2);
        
        Date now3 = DateHelper.parseDateTime("2004-02-16 12:30");
        
        IntervalTo result3 = schedule.nextDue(
        		new ScheduleContext(now3));
        
        assertEquals(expected, result3);
    }
    
    public void testWithTime() throws ParseException {
    	
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setOn(5);
        
    	TimeSchedule time = new TimeSchedule();
    	time.setAt("10:00");
    	schedule.setRefinement(time);
    	
    	// A Tuesday.
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDate("2005-11-01"));
    	
    	IntervalTo nextDue = schedule.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2005-11-04 10:00"));
    	
    	assertEquals(expected, nextDue);
    }
    
    public void testDefaultFrom() throws ParseException {
		
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setTo(2);

        // A Friday.
        IntervalTo result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-03")));
        
        // Result should be from Monday to end Tuesday.
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2006-03-06 00:00"), 
        		DateHelper.parseDateTime("2006-03-08 00:00"));
        
        assertEquals(expected, result);
        
        result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-07")));
        
        assertEquals(expected, result);
    }

    public void testDefaultTo() throws ParseException {
		
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setFrom(2);

        // 8th March 2006 was a Wednesday
        IntervalTo result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-06")));
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2006-03-07 00:00"), 
        		DateHelper.parseDateTime("2006-03-13 00:00"));
        
        assertEquals(expected, result);
        
        result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-12")));
        
        assertEquals(expected, result);
    }
    
    public void testInclusive() throws ParseException {
		
        DayOfWeekSchedule schedule = new DayOfWeekSchedule();
        schedule.setTo(2);

        IntervalTo result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-07 10:15")));
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2006-03-06 00:00"), 
        		DateHelper.parseDateTime("2006-03-08 00:00"));
        
        assertEquals(expected, result);
    	
    }

    public void testWithOverMidnightTime() throws ParseException {
    	
		DayOfWeekSchedule test = new DayOfWeekSchedule(); 

		test.setOn(3);

		TimeSchedule time = new TimeSchedule();

		time.setFrom("23:00");

		time.setTo("01:00");

		test.setRefinement(time);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-11 00:00"));

		IntervalTo result = test.nextDue(context);

        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2009-02-11 23:00"), 
        		DateHelper.parseDateTime("2009-02-12 01:00"));
		
        assertEquals(expected,
        		result);
        
        context = context.move(
        		DateHelper.parseDateTime("2009-02-12 00:00"));
        
		result = test.nextDue(context);

        assertEquals(expected,
        		result);
    }
    
    public void testOnExample() throws ArooaParseException, ParseException {
    	
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DayOfWeekOnExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	IntervalTo next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-03-07 12:00")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-03-08 00:00"),
    			DateHelper.parseDateTime("2011-03-09 00:00"));
    	
    	assertEquals(expected, next);
    }
    
    public void testBetweenExample() throws ArooaParseException, ParseException {
    	
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DayOfWeekBetweenExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	IntervalTo next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-03-07 12:00")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-03-07 15:45"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-03-07 15:46")));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-03-11 15:45"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-03-11 15:46")));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-03-12 15:45"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-03-13 15:46")));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-03-14 15:45"));
    	
    	assertEquals(expected, next);
    }
}
