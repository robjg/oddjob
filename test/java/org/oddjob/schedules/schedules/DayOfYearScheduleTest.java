/*
 * (c) Rob Gordon 2005
 */
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

/**
 * 
 */
public class DayOfYearScheduleTest extends TestCase {

	public void testNextDue1() throws ParseException {
		DayOfYearSchedule s = new DayOfYearSchedule();
		s.setFrom("02-05");
		s.setTo("02-25");
		
		ScheduleContext c = new ScheduleContext(
				DateHelper.parseDate("2003-02-02"));
		
		Interval result = s.nextDue(c);

		IntervalTo expected = 
			new IntervalTo(DateHelper.parseDate("2003-02-05"),
					DateHelper.parseDate("2003-02-26"));

		assertEquals(expected, result);
		
		c = new ScheduleContext(
				DateHelper.parseDate("2003-02-15"));
		
		result = s.nextDue(c);

		expected = new IntervalTo(DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-26"));
		
		assertEquals(expected, result);
		
		c = new ScheduleContext(
				DateHelper.parseDate("2003-02-15"));
		
		result = s.nextDue(c);

		expected = new IntervalTo(DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-26"));
		
		assertEquals(expected, result);
		
		c = new ScheduleContext(
				DateHelper.parseDate("2003-02-27"));
		result = s.nextDue(c);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2004-02-05"),
				DateHelper.parseDate("2004-02-26"));
		
		assertEquals(expected, result);
	}


	public void testOverYearBoundary() throws ParseException {
		DayOfYearSchedule s = new DayOfYearSchedule();
		s.setFrom("12-17");
		s.setTo("01-04");
		
		ScheduleContext c = new ScheduleContext(
				DateHelper.parseDate("2011-01-02"));
		Interval result = s.nextDue(c);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2010-12-17"),
				DateHelper.parseDate("2011-01-05"));
		
		assertEquals(expected, result);
	}
	
	public void test29thFeb() throws ParseException {
		
		DayOfYearSchedule test = new DayOfYearSchedule();
		test.setOn("02-29");
		
		ScheduleContext c = new ScheduleContext(
				DateHelper.parseDate("2008-01-01"));
		Interval result = test.nextDue(c);

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2008-02-29"),
				DateHelper.parseDate("2008-03-01"));
		
		assertEquals(expected, result);
	}
	
	public void testFromToExample() throws ArooaParseException, ParseException {
		
		OddjobDescriptorFactory df = new OddjobDescriptorFactory();

		ArooaDescriptor descriptor = df.createDescriptor(
				getClass().getClassLoader());

		StandardFragmentParser parser = new StandardFragmentParser(descriptor);

		parser.parse(new XMLConfiguration(
				"org/oddjob/schedules/schedules/DayOfYearFromTo.xml", 
				getClass().getClassLoader()));

		Schedule schedule = (Schedule)	parser.getRoot();

		IntervalTo next = schedule.nextDue(new ScheduleContext(
				DateHelper.parseDate("2010-02-15")));

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2010-12-25 00:00"),
				DateHelper.parseDateTime("2010-12-27 00:00"));
		
		assertEquals(expected, next);
	}
	
	public void testOnExample() throws ArooaParseException, ParseException {
		
		OddjobDescriptorFactory df = new OddjobDescriptorFactory();

		ArooaDescriptor descriptor = df.createDescriptor(
				getClass().getClassLoader());

		StandardFragmentParser parser = new StandardFragmentParser(descriptor);

		parser.parse(new XMLConfiguration(
				"org/oddjob/schedules/schedules/DayOfYearOn.xml", 
				getClass().getClassLoader()));

		Schedule schedule = (Schedule)	parser.getRoot();

		IntervalTo next = schedule.nextDue(new ScheduleContext(
				DateHelper.parseDate("2010-02-15")));

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2011-01-01 00:00"),
				DateHelper.parseDateTime("2011-01-02 00:00"));
		
		assertEquals(expected, next);
	}
}
