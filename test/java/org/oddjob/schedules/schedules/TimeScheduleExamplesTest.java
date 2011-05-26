package org.oddjob.schedules.schedules;

import java.text.ParseException;

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

public class TimeScheduleExamplesTest extends TestCase {
	
    public void testSimpleExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/TimeScheduleSimpleExample.xml", 
    			getClass().getClassLoader()));
    	
    	TimeSchedule schedule = (TimeSchedule)	parser.getRoot();

    	assertEquals("10:00", schedule.getFrom());
    	assertEquals("14:00", schedule.getTo());
    	
    	
    	IntervalTo next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-02-15 16:05")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-02-16 10:00"),
    			DateHelper.parseDateTime("2011-02-16 14:00"));
    	
    	assertEquals(expected, next);
    }

    
    public void testTimeAndIntervalExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/TimeAndIntervalExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	IntervalTo next = schedule.nextDue(new ScheduleContext(
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
