/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.schedules.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.SimpleScheduleResult;
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.schedules.units.WeekOfMonth;

/**
 *
 * @author Rob Gordon.
 */
public class MonthlyScheduleTest extends TestCase {
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
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setFromDay(new DayOfMonth.Number(5));
        schedule.setToDay(new DayOfMonth.Number(25));

        Date now1 = inputFormat.parse("10-feb-2003 12:30");
        
        IntervalTo expected = new IntervalTo(
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
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setFromDay(new DayOfMonth.Number(5));
        schedule.setToDay(new DayOfMonth.Number(25));
        
        Date now1 = inputFormat.parse("26-feb-2003 12:30");
        
        IntervalTo expected = new IntervalTo(
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
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setFromDay(new DayOfMonth.Number(25));
        schedule.setToDay(new DayOfMonth.Number(5));
        
        Date now1 = inputFormat.parse("26-feb-2003 12:30");
        
        IntervalTo expected = new IntervalTo(
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
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setFromDay(new DayOfMonth.Number(5));
        schedule.setToDay(DayOfMonth.Shorthands.LAST);
        
        Date now1 = inputFormat.parse("02-mar-2003 12:30");
        
        Interval interval1 = schedule.nextDue(
        		new ScheduleContext(now1));
        
        IntervalTo expected1 = new IntervalTo(
                DateHelper.parseDateTime("2003-03-05"),
                DateHelper.parseDateTime("2003-04-01"));
        
        assertTrue("interval as expected", interval1.equals(expected1));

        schedule.setToDay(DayOfMonth.Shorthands.PENULTIMATE);
        Interval interval2 = schedule.nextDue(
        		new ScheduleContext(now1));
        
        IntervalTo expected2 = new IntervalTo(
        		DateHelper.parseDateTime("2003-03-05"),
                DateHelper.parseDateTime("2003-03-31"));
        
        assertEquals(expected2, interval2);        
    }
    
    public void testDefaultFrom() throws ParseException {
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setToDay(new DayOfMonth.Number(25));

        Interval result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-26")));
        
        assertEquals(new IntervalTo(
        		DateHelper.parseDateTime("2006-04-01 00:00"), 
        		DateHelper.parseDateTime("2006-04-26 00:00")),
        		result);
    }

    public void testDefaultTo() throws ParseException {
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setFromDay(new DayOfMonth.Number(5));

        Interval result = schedule.nextDue(
        		new ScheduleContext(
        				DateHelper.parseDate("2006-03-26")));
        
        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2006-03-05 00:00"), 
        		DateHelper.parseDateTime("2006-04-01 00:00"));
        
        assertEquals(expected, result);
    }
    
    public void testInclusive() throws ParseException {
        MonthlySchedule schedule = new MonthlySchedule();
        schedule.setToDay(new DayOfMonth.Number(25));

        Interval result = schedule.nextDue(
        		new ScheduleContext(DateHelper.parseDate("2006-03-25 10:15")));
        
        assertEquals(new IntervalTo(
        		DateHelper.parseDateTime("2006-03-01 00:00"), 
        		DateHelper.parseDateTime("2006-03-26 00:00")),
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
    			expected.getToDate()));
    	
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
    			expected.getToDate()));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-05-15 00:00"),
    			DateHelper.parseDateTime("2011-05-16 00:00"));
    	
    	assertEquals(expected, next);
    }
    
    public void testShiftFromCalendar() throws ParseException {
    	
    	MonthlySchedule test = new MonthlySchedule();
    	test.setOnDayOfWeek(DayOfWeek.Days.FRIDAY);
    	test.setInWeek(WeekOfMonth.Weeks.LAST);
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.clear();
    	// Friday 25th March 2011
    	calendar.set(2011, 2, 25);
    	
    	Calendar result = test.shiftFromCalendar(calendar, 1);
    	
    	assertEquals(
    			DateHelper.parseDate("2011-4-29"),
    			result.getTime());
    }
    
    public void testDayOfWeekInMonth() throws ParseException {
    	
    	MonthlySchedule test = new MonthlySchedule();
    	test.setOnDayOfWeek(DayOfWeek.Days.FRIDAY);
    	test.setInWeek(WeekOfMonth.Weeks.LAST);
    	
    	TimeSchedule time = new TimeSchedule();
    	time.setAt("07:00");
    	
    	test.setRefinement(time);
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2011-03-25 12:00"));
    	
    	Interval result = test.nextDue(context);
    	
    	ScheduleResult expected = new SimpleScheduleResult(
    			new IntervalTo(
    					DateHelper.parseDateTime("2011-04-29 07:00")));
    	
    	assertEquals(expected, result);
    }
    
    public void testDayOfWeekInMonthOverBoundry() throws ParseException {
    	
    	MonthlySchedule test = new MonthlySchedule();
    	test.setFromDayOfWeek(DayOfWeek.Days.FRIDAY);
    	test.setFromWeek(WeekOfMonth.Weeks.LAST);
    	test.setToDayOfWeek(DayOfWeek.Days.MONDAY);
    	test.setToWeek(WeekOfMonth.Weeks.FIRST);
    	
    	DailySchedule time = new DailySchedule();
    	time.setAt("07:00");
    	
    	test.setRefinement(time);
    	
    	Interval[] results = new ScheduleRoller(test, 12).resultsFrom(
    			DateHelper.parseDateTime("2011-03-25 12:00"));
    	    	
    	assertEquals(new IntervalTo(
    			DateHelper.parseDateTime("2011-03-26 07:00")), 
    			results[0]);
    	
    	assertEquals(new IntervalTo(
    			DateHelper.parseDateTime("2011-03-27 07:00")), 
    			results[1]);
    	
    	assertEquals(new IntervalTo(
    			DateHelper.parseDateTime("2011-04-04 07:00")), 
    			results[9]);
    	
    	assertEquals(new IntervalTo(
    			DateHelper.parseDateTime("2011-04-29 07:00")), 
    			results[10]);
    }
    
    public void testLastFridayOfMonth() throws ParseException, ArooaParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/LastFridayOfMonth.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleRoller roller = new ScheduleRoller(schedule, 12);

    	Interval[] results = roller.resultsFrom(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	ScheduleResult expected;
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    			DateHelper.parseDateTime("2011-04-29 07:00")));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    					DateHelper.parseDateTime("2011-05-27 07:00")));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    			DateHelper.parseDateTime("2011-06-24 07:00")));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    			DateHelper.parseDateTime("2011-07-29 07:00")));
    	
    	assertEquals(expected, results[3]);
    }
    
    public void testToString() {
    	MonthlySchedule test = new MonthlySchedule();
    	test.setOnDayOfWeek(DayOfWeek.Days.FRIDAY);
    	test.setInWeek(WeekOfMonth.Weeks.LAST);

    	String expected = "Monthly on week LAST, day FRIDAY";
    	
    	assertEquals(expected, test.toString());
    	
    	test = new MonthlySchedule();
    	test.setFromDay(new DayOfMonth.Number(5));
    	test.setToDay(new DayOfMonth.Number(15));
    	
    	expected = "Monthly from day 5 to day 15";
    	
    	assertEquals(expected, test.toString());
    	
    	test = new MonthlySchedule();
    	
    	expected = "Monthly from the start of the month to the end of the month";
    	
    	assertEquals(expected, test.toString());
    	
    	test = new MonthlySchedule();
    	test.setInWeek(WeekOfMonth.Weeks.FIRST);
    	
    	TimeSchedule time = new TimeSchedule();
    	time.setAt("07:00");
    	
    	test.setRefinement(time);
    	
    	expected = "Monthly on week FIRST with refinement Time at 07:00";
    	
    	assertEquals(expected, test.toString());
    }
    
    /**
     * Trying to understand day of week in month stuff.
     */
    public static void main(String... args) throws ParseException {

    	Calendar cal = Calendar.getInstance();
    	System.out.println(cal.getMinimalDaysInFirstWeek());
    	System.out.println(cal.getFirstDayOfWeek());
    	
    	YearlySchedule test = new YearlySchedule();
//    	DayOfMonthSchedule test = new DayOfMonthSchedule();
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDate("2011-09-13"));
    	
    	Interval month = test.nextDue(context);
    	
    	TimeSchedule daily = new TimeSchedule();
    	
    	context = context.spawn(month.getFromDate(), month);
    		
    	while (true) {
    		
    		Interval next = daily.nextDue(context);
    		if (next == null) {
    			break;
    		}
    		    		
    		cal.setTime(next.getFromDate());
    		System.out.println(next.getFromDate() + " " + cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) 
    				+ " " + cal.get(Calendar.DAY_OF_WEEK) + " " + cal.get(Calendar.WEEK_OF_MONTH));
    		
    		context = context.move(next.getToDate());
    	}
    	
    	
    }
}
