/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.text.ParseException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.schedules.WeeklySchedule;
import org.oddjob.schedules.schedules.TimeSchedule;
import org.oddjob.schedules.units.DayOfWeek;

/**
 * 
 */
public class ScheduleListTest extends TestCase {
		
	private static final Logger logger = Logger.getLogger(ScheduleListTest.class);
	
	protected void setUp() {
		logger.debug("============== " + getName() + " ==================");
	}
	
	public void testTwoTimes() throws ParseException {
		
		TimeSchedule s1 = new TimeSchedule();
		s1.setFrom("10:00");
		s1.setTo("11:00");
		
		TimeSchedule s2 = new TimeSchedule();
		s2.setFrom("22:00");
		s2.setTo("02:00");
		
		ScheduleList test = new ScheduleList();
		test.setSchedules(new Schedule[] { s1, s2 });
		
		ScheduleContext context; 
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2003-08-15 00:00")); 		

		Interval result = test.nextDue(context);
		
		IntervalBase expected = new IntervalTo(
				DateHelper.parseDateTime("2003-08-14 22:00"),
				DateHelper.parseDateTime("2003-08-15 02:00"));

		assertEquals(expected, result);	
		
		result = test.nextDue(context.move(
				result.getToDate()));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2003-08-15 10:00"),
				DateHelper.parseDateTime("2003-08-15 11:00"));

		assertEquals(expected, result);	
	}
	
	public void testTwoTimesAsRefinement() throws ParseException {
		
		TimeSchedule s1 = new TimeSchedule();
		s1.setFrom("10:00");
		s1.setTo("11:00");
		
		TimeSchedule s2 = new TimeSchedule();
		s2.setFrom("22:00");
		s2.setTo("02:00");
		
		ScheduleList test = new ScheduleList();
		test.setSchedules(new Schedule[] { s1, s2 });
		
		WeeklySchedule dayOfWeek = new WeeklySchedule();
		dayOfWeek.setOn(DayOfWeek.Days.THURSDAY);

		dayOfWeek.setRefinement(test);
		
		ScheduleContext context; 
		
		context = new ScheduleContext(
				DateHelper.parseDateTime("2003-08-14 00:00")); 		

		Interval result = dayOfWeek.nextDue(context);
		
		IntervalBase expected;
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2003-08-14 10:00"),
				DateHelper.parseDateTime("2003-08-14 11:00"));

		assertEquals(expected, result);	
		
		result = dayOfWeek.nextDue(context.move(
				result.getToDate()));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2003-08-14 22:00"),
				DateHelper.parseDateTime("2003-08-15 02:00"));

		assertEquals(expected, result);	
	}

	
	public void testEmpty() throws ParseException {
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2003-08-14 22:00"),
				DateHelper.parseDateTime("2003-08-15 02:00"));

		ScheduleList test = new ScheduleList();
		test.setSchedules(new Schedule[] { });
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2003-08-15 00:00")); 		
		context = context.spawn(expected);
		
		assertEquals(null, test.nextDue(context));
		
	}
	
    public void testListExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/ScheduleListExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDate("2011-04-08")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-04-08"), 
    			DateHelper.parseDateTime("2011-04-09"));
    	
    	assertEquals(expected, next);
    }
}
