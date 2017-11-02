package org.oddjob.schedules.schedules;

import org.junit.Test;

import java.text.ParseException;

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

public class DailyScheduleExamplesTest extends OjTestCase {
	
   @Test
    public void testSimpleExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DailyScheduleSimpleExample.xml", 
    			getClass().getClassLoader()));
    	
    	DailySchedule schedule = (DailySchedule)	parser.getRoot();

    	assertEquals("10:00", schedule.getFrom());
    	assertEquals("10:00", schedule.getTo());
    	
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-02-15 16:05")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-02-16 10:00"));
    	
    	assertEquals(expected, next);
    }

    
   @Test
    public void testTimeAndIntervalExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DailyWithIntervalExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-02-15 16:05")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-02-15 22:00"),     			
    			DateHelper.parseDateTime("2011-02-15 22:15"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-02-16 03:55")));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-02-16 03:45"),     			
    			DateHelper.parseDateTime("2011-02-16 04:00"));
    	
    	assertEquals(expected, next);
    	
    }
}
