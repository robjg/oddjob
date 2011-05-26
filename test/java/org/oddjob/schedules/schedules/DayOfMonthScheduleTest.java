/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.schedules.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

/**
 *
 * @author Rob Gordon.
 */
public class DayOfMonthScheduleTest extends TestCase {
//	private static Logger logger = Logger.getLogger(DayOfMonthScheduleTest.class); 
	
    DateFormat checkFormat;
    DateFormat inputFormat;
    
    protected void setUp() {
    	checkFormat = new SimpleDateFormat("dd-MMM-yy HH:mm:ss:SSS");
    	inputFormat = new SimpleDateFormat("dd-MMM-yy HH:mm");
    }

    /**
     * Test the next due time when a date is between the from 
     * and the to date.
     */
    public void testFromAndTo() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setFrom("5");
        schedule.setTo("25");

        Date now1 = inputFormat.parse("10-feb-2003 12:30");
        
        Interval expected = new IntervalTo(
        		DateHelper.parseDate("2003-02-05"),
        		DateHelper.parseDate("2003-02-26"));

        Interval result = schedule.nextDue(new ScheduleContext(now1));
        
        assertEquals(expected, result);
    }
    
    /**
     * Test the next due time when a date is after
     * the to time.
     */
    public void testAfter() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setFrom("5");
        schedule.setTo("25");
        
        Date now1 = inputFormat.parse("26-feb-2003 12:30");
        
        Interval expected = new IntervalTo(
        		DateHelper.parseDateTime("2003-03-05"),
        		DateHelper.parseDateTime("2003-03-26"));
        		
        Interval result = schedule.nextDue(new ScheduleContext(now1)); 

        assertEquals(expected, result);        
        
    }
    
    /**
     * Test the next due when the from and to span
     * a month boundry.
     */
    public void testOverBoundry() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setFrom("25");
        schedule.setTo("5");
        
        Date now1 = inputFormat.parse("26-feb-2003 12:30");
        
        Interval expected = new IntervalTo(
        		DateHelper.parseDate("2003-02-25"),
        		DateHelper.parseDate("2003-03-06"));
        
        Interval result1 = schedule.nextDue(new ScheduleContext(now1));
        
        assertEquals(expected, result1);
        

        Date now2 = inputFormat.parse("24-feb-2003 12:30");
        
        Interval result2 = schedule.nextDue(new ScheduleContext(now2));
        
        assertEquals(expected, result2);
        
        Date now3 = inputFormat.parse("1-mar-2003 12:30");
        
        Interval result3 = schedule.nextDue(new ScheduleContext(now3));

        assertEquals(expected, result3);
    }

    /**
     * Test last day of the month.
     */
    public void testLastDay() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setFrom("5");
        schedule.setTo("0");
        
        Date now1 = inputFormat.parse("02-mar-2003 12:30");
        Interval interval1 = schedule.nextDue(
        		new ScheduleContext(now1));
        Interval expected1 = new Interval(
                checkFormat.parse("05-mar-2003 00:00:00:000"),
                checkFormat.parse("31-mar-2003 23:59:59:999"));
        System.out.println("interval1: " + interval1);
        assertTrue("interval as expected", interval1.equals(expected1));

        schedule.setTo("-1");
        Interval interval2 = schedule.nextDue(
        		new ScheduleContext(now1));
        Interval expected2 = new Interval(
                checkFormat.parse("05-mar-2003 00:00:00:000"),
                checkFormat.parse("30-mar-2003 23:59:59:999"));
        System.out.println("interval2: " + interval2);
        assertTrue("interval as expected", interval2.equals(expected2));        
    }
    
    public void testDefaultFrom() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setTo("25");

        Interval result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-26")));
        
        assertEquals(new Interval(
        		DateHelper.parseDateTime("2006-04-01 00:00"), 
        		DateHelper.parseDateTime("2006-04-25 23:59:59:999")),
        		result);
    }

    public void testDefaultTo() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setFrom("5");

        Interval result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-26")));
        
        assertEquals(new Interval(
        		DateHelper.parseDateTime("2006-03-05 00:00"), 
        		DateHelper.parseDateTime("2006-03-31 23:59:59:999")),
        		result);
    }
    
    public void testInclusive() throws ParseException {
        DayOfMonthSchedule schedule = new DayOfMonthSchedule();
        schedule.setTo("25");

        Interval result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-25 10:15")));
        
        assertEquals(new Interval(
        		DateHelper.parseDateTime("2006-03-01 00:00"), 
        		DateHelper.parseDateTime("2006-03-25 23:59:59:999")),
        		result);
    	
    }
    
    public void testDayOfMonthExample1() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DayOfMonthExample1.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	Interval next = schedule.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-17 10:00"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(context.move(
    			expected.getUpToDate()));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-18 10:00"));
    	
    	assertEquals(expected, next);
    }
    
    public void testDayOfMonthExample2() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DayOfMonthExample2.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	Interval next = schedule.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-15 00:00"),
    			DateHelper.parseDateTime("2011-04-16 00:00"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(context.move(
    			expected.getUpToDate()));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-05-15 00:00"),
    			DateHelper.parseDateTime("2011-05-16 00:00"));
    	
    	assertEquals(expected, next);
    }
}
