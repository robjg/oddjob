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
import org.oddjob.schedules.ScheduleList;

public class LastScheduleTest extends TestCase {

	
	public void testLastChristmas() throws ParseException {
		
		DateSchedule c1 = new DateSchedule();
		c1.setOn("2009-12-25");
		
		DateSchedule c2 = new DateSchedule();
		c2.setOn("2008-12-25");
		
		DateSchedule c3 = new DateSchedule();
		c3.setOn("2007-12-25");
		
		ScheduleList list = new ScheduleList();
		list.setSchedules(new Schedule[] {
			c1, c2, c3 });
		
		LastSchedule last = new LastSchedule();
		last.setRefinement(list);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2003-06-21"));
		
		IntervalTo result = last.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2009-12-25 00:00"),
				DateHelper.parseDateTime("2009-12-26 00:00"));

		assertEquals(expected, result);
	}
	
	public void testNever() throws ParseException {
		
		DateSchedule c1 = new DateSchedule();
		c1.setOn("2009-12-25");
		
		LastSchedule last = new LastSchedule();
		last.setRefinement(c1);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-12-26"));
		
		IntervalTo result = last.nextDue(context);
		
		assertNull(result);
	}
	
	public void testLastDayOfApril() throws ParseException {
		
		LastSchedule test = new LastSchedule();

		MonthSchedule month = new MonthSchedule();
		month.setIn(4);
		
		TimeSchedule time = new TimeSchedule();
		time.setFrom("00:00");
		time.setTo("23:59");

		month.setRefinement(test);
		test.setRefinement(time);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2009-04-30 12:57"));
		
		IntervalTo result = month.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2009-04-30 00:00"),
				DateHelper.parseDateTime("2009-04-30 23:59"));

		assertEquals(expected, result);
	}
	
    public void testLastExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/LastExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleContext context = new ScheduleContext(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	IntervalTo next = schedule.nextDue(context);
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-27 00:00"),
    			DateHelper.parseDateTime("2011-04-28 00:00"));
    	
    	assertEquals(expected, next);
    	
    	next = schedule.nextDue(context.move(
    			expected.getUpToDate()));
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-05-31 00:00"),
    			DateHelper.parseDateTime("2011-06-01 00:00"));
    	
    	assertEquals(expected, next);
    }
}
