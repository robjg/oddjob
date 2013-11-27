/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.OddjobTestHelper;
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
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.SimpleInterval;
import org.oddjob.schedules.SimpleScheduleResult;

/**
 * 
 */
public class CountScheduleTest extends TestCase {
	
	private static class Counter implements Schedule {
		int count;
		public IntervalTo nextDue(ScheduleContext context) {
			count++;
			return new IntervalTo(new Date(0));
		}
	}
	
	public void testCount() {
		
		CountSchedule test = new CountSchedule();
		test.setCount(3);
		
		Counter counter = new Counter();
		test.setRefinement(counter);
		
		Interval nextDue = null;
		
		ScheduleContext context = new ScheduleContext(new Date());
		do {
			context = context.move(
					new Date(context.getDate().getTime() + 1));
			nextDue = test.nextDue(context);
		} while (nextDue != null);
		
		assertEquals(3, counter.count);
		
		nextDue = test.nextDue(context);
		
		assertEquals(null, nextDue);
	}
	
	public void testWithParentInterval() throws ParseException {
				
		CountSchedule test = new CountSchedule();
		test.setCount(3);
		
		Counter counter = new Counter();
		test.setRefinement(counter);
		
		Interval nextDue = null;
		
		ScheduleContext context = new ScheduleContext(new Date());
		context = context.spawn(new SimpleInterval(
				DateHelper.parseDateTime("2011-04-12 00:00"),
				DateHelper.parseDateTime("2011-04-13 00:00")));
		
		do {
			nextDue = test.nextDue(context);
		} while (nextDue != null);
		
		assertEquals(3, counter.count);
		
		context = context.spawn(new SimpleInterval(
				DateHelper.parseDateTime("2011-04-13 00:00"),
				DateHelper.parseDateTime("2011-04-14 00:00")));
		
		do {
			nextDue = test.nextDue(context);
		} while (nextDue != null);
		
		assertEquals(6, counter.count);
	}
	
	public void testHowManyNextDues() {

		int count = 0;
		
		CountSchedule test = new CountSchedule(3);
		
		ScheduleContext context = new ScheduleContext(new Date());
		
		while (test.nextDue(context) != null) {
			context = context.move(new Date(
					context.getDate().getTime() + 1));
			++count;
		}
		
		assertEquals(3, count);
	}
	
	/**
	 * Test that a CountSchedule can be serialized.
	 * 
	 * @throws Exception
	 */
	public void testSerialize() throws Exception {
		CountSchedule test = new CountSchedule();
		test.setCount(1);

		Date now = new Date();
		
		Map<Object, Object> map = new HashMap<Object, Object>();
		ScheduleContext context = new ScheduleContext(
				now, null, map);
		
		Interval interval = test.nextDue(context);
		assertNotNull(interval);
		
		Schedule copy = (Schedule) OddjobTestHelper.copy(test);
		
		context = new ScheduleContext(
				new Date(now.getTime() + 1), null, map);
		
		interval = copy.nextDue(context);
		assertEquals(null, interval);
		
	}
	
	public void testCountExample() throws ArooaParseException, ParseException {
    	
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/CountExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval[] results = new ScheduleRoller(schedule).resultsFrom(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	ScheduleResult expected;
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:00"),
    					DateHelper.parseDateTime("2011-04-12 11:15")));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:15"),
    					DateHelper.parseDateTime("2011-04-12 11:30")));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:30"),
    					DateHelper.parseDateTime("2011-04-12 11:45")));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:45"),
    					DateHelper.parseDateTime("2011-04-12 12:00")));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 12:00"),
    					DateHelper.parseDateTime("2011-04-12 12:15")));
    	
    	assertEquals(expected, results[4]);
    	
    	expected = null;
    	
    	assertEquals(expected, results[5]);
	}
	
	public void testCountDaily() throws ArooaParseException, ParseException {
    	
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/CountDaily.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval[] results = new ScheduleRoller(schedule, 100).resultsFrom(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	ScheduleResult expected;
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:00"),
    					DateHelper.parseDateTime("2011-04-12 11:05")));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:05"),
    					DateHelper.parseDateTime("2011-04-12 11:10")));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:10"),
    					DateHelper.parseDateTime("2011-04-12 11:15")));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-13 11:00"),
    					DateHelper.parseDateTime("2011-04-13 11:05")));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-13 11:05"),
    					DateHelper.parseDateTime("2011-04-13 11:10")));
    	
    	assertEquals(expected, results[4]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-13 11:10"),
    					DateHelper.parseDateTime("2011-04-13 11:15")));
    	
    	assertEquals(expected, results[5]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-14 11:00"),
    					DateHelper.parseDateTime("2011-04-14 11:05")));
    	
    	assertEquals(expected, results[6]);
	}
	
	public void testCountDifferentCounts() throws ArooaParseException, ParseException {
    	
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/CountDifferentCounts.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	Interval[] results = new ScheduleRoller(schedule, 100).resultsFrom(
    			DateHelper.parseDateTime("2011-04-12 11:00"));
    	
    	ScheduleResult expected;
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:00"),
    					DateHelper.parseDateTime("2011-04-12 11:01")));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-12 11:01"),
    					DateHelper.parseDateTime("2011-04-12 11:02")));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-13 11:00"),
    					DateHelper.parseDateTime("2011-04-13 11:01")));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-13 11:01"),
    					DateHelper.parseDateTime("2011-04-13 11:02")));
    	
    	assertEquals(expected, results[3]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-14 11:00"),
    					DateHelper.parseDateTime("2011-04-14 11:01")));
    	
    	assertEquals(expected, results[4]);
    	
    	expected = new SimpleScheduleResult(
    			new SimpleInterval(
    					DateHelper.parseDateTime("2011-04-14 11:01"),
    					DateHelper.parseDateTime("2011-04-14 11:02")));
    	
    	assertEquals(expected, results[5]);
    	
    	expected = null;
    	
    	assertEquals(expected, results[6]);
	}
}
