/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.schedules.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 *
 * @author Rob Gordon.
 */
public class AfterScheduleTest extends TestCase {

    static DateFormat checkFormat = new SimpleDateFormat("dd-MMM-yy HH:mm:ss:SSS");
    static DateFormat inputFormat = new SimpleDateFormat("dd-MMM-yy HH:mm");
    
    public void testAfterInterval() throws ParseException {
    	
        AfterSchedule after = new AfterSchedule();
        
        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:10");
        
        after.setSchedule(interval);

        ScheduleResult[] results = new ScheduleRoller(
        		after).resultsFrom(
        				DateHelper.parseDateTime("2000-01-01 12:00"));

        ScheduleResult expected = new SimpleScheduleResult(
        		new SimpleInterval(
	        		DateHelper.parseDateTime("2000-01-01 12:10"),
	        		DateHelper.parseDateTime("2000-01-01 12:20")),
	        	DateHelper.parseDateTime("2000-01-01 12:10"));

        assertEquals(expected, results[0]);        
        
        expected = new SimpleScheduleResult(
        		new SimpleInterval(
	        		DateHelper.parseDateTime("2000-01-01 12:20"),
	        		DateHelper.parseDateTime("2000-01-01 12:30")),
	        	DateHelper.parseDateTime("2000-01-01 12:20"));

        assertEquals(expected, results[1]);        
    }
   
    public void testAfterEndOfSchedule() throws ParseException {
    	
        AfterSchedule after = new AfterSchedule();
        
        DateSchedule interval = new DateSchedule();
        interval.setOn("2011-09-20");
        
        after.setSchedule(interval);

        Interval[] results = new ScheduleRoller(
        		after).resultsFrom(
        				DateHelper.parseDateTime("2011-09-20 12:00"));

        ScheduleResult expected = new SimpleScheduleResult(
        	new IntervalTo(
        		DateHelper.parseDateTime("2011-09-21 00:00"),
        		IntervalTo.END_OF_TIME),
        	DateHelper.parseDateTime("2011-09-21 00:00"));

        assertEquals(expected, results[0]);        
        
        assertEquals(null, results[1]);        
    }
    
    public void testAfterExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/AfterScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	ScheduleResult next = schedule.nextDue(context);
    	
    	ScheduleResult expected = new SimpleScheduleResult(
    			new IntervalTo(
	    			DateHelper.parseDateTime("2011-04-12 11:20"),
	    			DateHelper.parseDateTime("2011-04-12 11:40")),
	    		DateHelper.parseDateTime("2011-04-12 11:20"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(context.move(
    			DateHelper.parseDate("2011-04-12 12:20:00:001")));
    	
    	assertEquals(null, next);
    }
    
	public void testAfterBusinessDays() throws ArooaParseException, ParseException {
    	
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/AfterBusinessDays.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleResult[] results = new ScheduleRoller(schedule).resultsFrom(
    			DateHelper.parseDateTime("2011-04-27 12:00"));
    	    	
    	Interval expected = new SimpleScheduleResult(
    			new SimpleInterval(
	    			DateHelper.parseDateTime("2011-04-28 08:00"),
	    			DateHelper.parseDateTime("2011-04-29 00:00")),
    			DateHelper.parseDateTime("2011-04-28 00:00"));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-29 08:00"),
    					DateHelper.parseDateTime("2011-04-30 00:00")),
    			DateHelper.parseDateTime("2011-04-29 00:00"));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-30 08:00"),
    					DateHelper.parseDateTime("2011-05-04 00:00")),
    			DateHelper.parseDateTime("2011-04-30 00:00"));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-05-04 08:00"),
    					DateHelper.parseDateTime("2011-05-05 00:00")),
    			DateHelper.parseDateTime("2011-05-04 00:00"));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-05-05 08:00"),
    					DateHelper.parseDateTime("2011-05-06 00:00")),
    			DateHelper.parseDateTime("2011-05-05 00:00"));
    	
    	assertEquals(expected, results[4]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-05-06 08:00"),
    					DateHelper.parseDateTime("2011-05-07 00:00")),
    			DateHelper.parseDateTime("2011-05-06 00:00"));
    	
    	assertEquals(expected, results[5]);
	}
}
