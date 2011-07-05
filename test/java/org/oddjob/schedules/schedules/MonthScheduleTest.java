/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

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
public class MonthScheduleTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		TimeZone.setDefault(null);
	}
	
    public void testFromAndTo() throws ParseException {
        MonthSchedule schedule = new MonthSchedule();
        schedule.setFrom(2);
        schedule.setTo(4);
        
        Date now1 = DateHelper.parseDateTime("2003-02-10 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDate("2003-02-01"),
        		DateHelper.parseDate("2003-05-01"));
        
        IntervalTo result = schedule.nextDue(
        		new ScheduleContext(now1));
        
        assertEquals(expected, result);
    }
    
    public void testAfter() throws ParseException {
        MonthSchedule schedule = new MonthSchedule();
        schedule.setFrom(2);
        schedule.setTo(4);
        
        Date now1 = DateHelper.parseDateTime("2003-06-21 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDate("2004-02-01"),
        		DateHelper.parseDate("2004-05-01"));
        
        IntervalTo result = schedule.nextDue(
        		new ScheduleContext(now1));

        assertEquals(expected, result);
        
    }
    
    public void testOverBoundry() throws ParseException {
        MonthSchedule schedule = new MonthSchedule();
        schedule.setFrom(11);
        schedule.setTo(2);
        
        Date now1 = DateHelper.parseDateTime("2003-12-25 12:30");
                
        ScheduleContext context1 = 
        		new ScheduleContext(now1);
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2003-11-01 00:00"), 
        		DateHelper.parseDateTime("2004-03-01 00:00"));
        
        IntervalTo result1 = schedule.nextDue(context1);
        
        assertEquals(expected, result1);
        
        Date now2 = DateHelper.parseDateTime("2003-09-21 12:30");
        
        ScheduleContext context2 = new ScheduleContext(now2);

        IntervalTo result2 = schedule.nextDue(context2);
        
        assertEquals(expected, result2);

        Date now3 = DateHelper.parseDateTime("2004-02-01 12:30");

        ScheduleContext context3 = new ScheduleContext(now3);
        
        IntervalTo result3 = schedule.nextDue(context3);

        assertEquals(expected, result3);
    }
    
    public void testWithTime() throws ParseException {

    	MonthSchedule test = new MonthSchedule();
    	test.setIn(1);
    	
    	TimeSchedule time = new TimeSchedule();
    	time.setAt("20:00");
    	
    	test.setRefinement(time);

    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2004-01-31 20:01"));

    	IntervalTo result = test.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2005-01-01 20:00"));
    		
    	assertEquals(expected, result);
    }
    
    public void testMonthExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/MonthScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	IntervalTo next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDate("2010-02-15")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2010-02-15 11:00"));
    	
    	assertEquals(expected, next);
    }
}
