package org.oddjob.schedules.schedules;

import java.text.ParseException;

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

public class DayAfterScheduleTest extends TestCase {

	public void testDayAfterScheduleExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DayAfterScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	Interval next = schedule.nextDue(context);
    	
    	// Not sure this is right.
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-30 02:00"),
    			DateHelper.parseDateTime("2011-05-01 02:00"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(context.move(
    			expected.getUpToDate()));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-06-01 02:00"),
    			DateHelper.parseDateTime("2011-06-02 02:00"));
    	
    	assertEquals(expected, next);
    	
	}
}
