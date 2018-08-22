package org.oddjob.schedules.schedules;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleList;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;
import org.oddjob.schedules.units.DayOfWeek;
import org.oddjob.schedules.units.Month;

public class DayAfterScheduleTest extends OjTestCase {

   @Test
	public void testDayAfterInterval() throws ParseException {
		
		DayAfterSchedule test = new DayAfterSchedule();
		
		ScheduleContext context = new ScheduleContext(new Date());
		context = context.spawn(new Date(), new IntervalTo(
				DateHelper.parseDate("2011-09-15"), 
				DateHelper.parseDate("2011-09-16")));
		
		Interval result = test.nextDue(context);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2011-09-16"), 
				DateHelper.parseDate("2011-09-17"));
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testDayAfterWithRefinement() throws ParseException {

		WeeklySchedule weekly = new WeeklySchedule();
		weekly.setOn(DayOfWeek.Days.WEDNESDAY);
		
		DayAfterSchedule test = new DayAfterSchedule();

		weekly.setRefinement(test);

		DailySchedule time = new DailySchedule();
		time.setFrom("14:00");
		
		test.setRefinement(time);

		Interval[] results = new ScheduleRoller(weekly).resultsFrom(
				DateHelper.parseDate("2011-09-15"));
				
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2011-09-15 14:00"), 
				DateHelper.parseDate("2011-09-16"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2011-09-22 14:00"), 
				DateHelper.parseDate("2011-09-23"));
		
		assertEquals(expected, results[1]);
	}
	
   @Test
	public void testDayAfterScheduleExample() throws ArooaParseException, ParseException {
    	
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	ScheduleList holidays = new ScheduleList();
    	YearlySchedule h1 = new YearlySchedule();
    	h1.setInMonth(Month.Months.AUGUST);
    	
    	DateSchedule h2 = new DateSchedule();
    	h2.setOn("2011-09-30");

    	holidays.setSchedules(0, h1);
    	holidays.setSchedules(1, h2);
    	
    	session.getBeanRegistry().register("holidays", holidays);
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/DayAfterScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval[] results = new ScheduleRoller(schedule).resultsFrom(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	ScheduleResult expected;
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-05-02 08:00")),
    			DateHelper.parseDateTime("2011-05-02 00:00"));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    					DateHelper.parseDateTime("2011-05-31 17:00")));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    					DateHelper.parseDateTime("2011-06-30 17:00")));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-09-01 08:00")),
    			DateHelper.parseDateTime("2011-09-01 00:00"));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    					DateHelper.parseDateTime("2011-10-03 08:00")),
    			DateHelper.parseDateTime("2011-10-03 00:00"));
    	
    	assertEquals(expected, results[4]);
    	
    	expected = new SimpleScheduleResult(
    			new IntervalTo(
    					DateHelper.parseDateTime("2011-10-31 17:00")));
    	
    	assertEquals(expected, results[5]);
	}
}
