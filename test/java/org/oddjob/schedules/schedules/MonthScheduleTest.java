/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.schedules.schedules;
import org.junit.Before;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

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
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.Month;

/**
 *
 * @author Rob Gordon.
 */
public class MonthScheduleTest extends OjTestCase {

    @Before
    public void setUp() throws Exception {
		TimeZone.setDefault(null);
	}
	
   @Test
    public void testFromAndTo() throws ParseException {
        YearlySchedule schedule = new YearlySchedule();
        schedule.setFromMonth(Month.Months.FEBRUARY);
        schedule.setToMonth(Month.Months.APRIL);
        
        Date now1 = DateHelper.parseDateTime("2003-02-10 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDate("2003-02-01"),
        		DateHelper.parseDate("2003-05-01"));
        
        Interval result = schedule.nextDue(
        		new ScheduleContext(now1));
        
        assertEquals(expected, result);
    }
    
   @Test
    public void testAfter() throws ParseException {
        YearlySchedule schedule = new YearlySchedule();
        schedule.setFromMonth(Month.Months.FEBRUARY);
        schedule.setToMonth(Month.Months.APRIL);
        
        Date now1 = DateHelper.parseDateTime("2003-06-21 12:30");
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDate("2004-02-01"),
        		DateHelper.parseDate("2004-05-01"));
        
        Interval result = schedule.nextDue(
        		new ScheduleContext(now1));

        assertEquals(expected, result);
        
    }
    
   @Test
    public void testOverBoundry() throws ParseException {
        YearlySchedule schedule = new YearlySchedule();
        schedule.setFromMonth(Month.Months.NOVEMBER);
        schedule.setToMonth(Month.Months.FEBRUARY);
        
        Date now1 = DateHelper.parseDateTime("2003-12-25 12:30");
                
        ScheduleContext context1 = 
        		new ScheduleContext(now1);
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2003-11-01 00:00"), 
        		DateHelper.parseDateTime("2004-03-01 00:00"));
        
        Interval result1 = schedule.nextDue(context1);
        
        assertEquals(expected, result1);
        
        Date now2 = DateHelper.parseDateTime("2003-09-21 12:30");
        
        ScheduleContext context2 = new ScheduleContext(now2);

        Interval result2 = schedule.nextDue(context2);
        
        assertEquals(expected, result2);

        Date now3 = DateHelper.parseDateTime("2004-02-01 12:30");

        ScheduleContext context3 = new ScheduleContext(now3);
        
        Interval result3 = schedule.nextDue(context3);

        assertEquals(expected, result3);
    }
    
   @Test
    public void testWithTime() throws ParseException {

    	YearlySchedule test = new YearlySchedule();
    	test.setInMonth(Month.Months.JANUARY);
    	
    	DailySchedule time = new DailySchedule();
    	time.setAt("20:00");
    	
    	test.setRefinement(time);

    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2004-01-31 20:01"));

    	Interval result = test.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2005-01-01 20:00"));
    		
    	assertEquals(expected, result);
    }
    
   @Test
    public void testMonthExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/MonthScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDate("2010-02-15")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2010-02-15 11:00"));
    	
    	assertEquals(expected, next);
    }
    
   @Test
    public void testMonthExample2() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/MonthScheduleExample2.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDate("2010-02-15")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2010-02-15 11:00"));
    	
    	assertEquals(expected, next);
    }
    
   @Test
    public void testYearlyInFebuaryIncludingLeapYears() throws ParseException {
    	
    	YearlySchedule test = new YearlySchedule();
    	test.setInMonth(Month.Months.FEBRUARY);
    	
    	ScheduleRoller roller = new ScheduleRoller(test);
    	ScheduleResult[] results = roller.resultsFrom(
    			DateHelper.parseDate("2006-01-01"));
    	
    	ScheduleResult expected;
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2006-02-01"),
    			DateHelper.parseDate("2006-03-01"));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2007-02-01"),
    			DateHelper.parseDate("2007-03-01"));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2008-02-01"),
    			DateHelper.parseDate("2008-03-01"));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2009-02-01"),
    			DateHelper.parseDate("2009-03-01"));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2010-02-01"),
    			DateHelper.parseDate("2010-03-01"));
    	
    	assertEquals(expected, results[4]);
    }
    
   @Test
    public void testLastDayInFebuaryIncludingLeapYears() throws ParseException {
    	
    	YearlySchedule test = new YearlySchedule();
    	test.setInMonth(Month.Months.FEBRUARY);
    	
    	MonthlySchedule monthly = new MonthlySchedule();
    	monthly.setOnDay(DayOfMonth.Shorthands.LAST);
    	
    	test.setRefinement(monthly);
    	
    	ScheduleRoller roller = new ScheduleRoller(test);
    	ScheduleResult[] results = roller.resultsFrom(
    			DateHelper.parseDate("2006-01-01"));
    	
    	ScheduleResult expected;
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2006-02-28"),
    			DateHelper.parseDate("2006-03-01"));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2007-02-28"),
    			DateHelper.parseDate("2007-03-01"));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2008-02-29"),
    			DateHelper.parseDate("2008-03-01"));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2009-02-28"),
    			DateHelper.parseDate("2009-03-01"));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDate("2010-02-28"),
    			DateHelper.parseDate("2010-03-01"));
    	
    	assertEquals(expected, results[4]);
    }
}
