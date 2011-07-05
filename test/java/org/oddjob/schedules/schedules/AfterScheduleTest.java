/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.schedules.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
public class AfterScheduleTest extends TestCase {

    static DateFormat checkFormat = new SimpleDateFormat("dd-MMM-yy HH:mm:ss:SSS");
    static DateFormat inputFormat = new SimpleDateFormat("dd-MMM-yy HH:mm");
    
    public void testAfterInterval() throws ParseException {
    	
        AfterSchedule after = new AfterSchedule();
        
        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:10");
        
        after.setRefinement(interval);

        IntervalTo next = after.nextDue(new ScheduleContext(
        		DateHelper.parseDateTime("2000-01-01 12:00")));

        IntervalTo expected = new IntervalTo(
        		DateHelper.parseDateTime("2000-01-01 12:10"));

        assertEquals(expected, next);        
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
    	
    	IntervalTo next = schedule.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-12 11:20"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(context.move(
    			DateHelper.parseDate("2011-04-12 12:20:00:001")));
    	
    	assertEquals(null, next);
    }
}
