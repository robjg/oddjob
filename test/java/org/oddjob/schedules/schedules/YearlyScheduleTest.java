/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
import org.oddjob.schedules.ScheduleResult;

/**
 * 
 */
public class YearlyScheduleTest extends OjTestCase {

   @Test
	public void testParseDay() throws ParseException {

		Date referenceDate = DateHelper.parseDate("2011-11-17");
		TimeZone timeZone = TimeZone.getDefault();
		
		Calendar result = YearlySchedule.parseDay("05-26", referenceDate, timeZone);
		
		assertEquals(DateHelper.parseDate("2011-05-26"), result.getTime());
		
		result = YearlySchedule.parseDay("5-26", referenceDate, timeZone);
		
		assertEquals(DateHelper.parseDate("2011-05-26"), result.getTime());
		
		result = YearlySchedule.parseDay("1-1", referenceDate, timeZone);
		
		assertEquals(DateHelper.parseDate("2011-01-01"), result.getTime());
		
		try {
			YearlySchedule.parseDay("-1", referenceDate, timeZone);
		}
		catch (ParseException e) {
			// Expected.
		}
	}
	
   @Test
	public void testNextDue1() throws ParseException {
		YearlySchedule test = new YearlySchedule();
		test.setFromDate("02-05");
		test.setToDate("02-25");
		
		ScheduleContext c = new ScheduleContext(
				DateHelper.parseDate("2003-02-02"));
		
		Interval result = test.nextDue(c);

		IntervalTo expected = 
			new IntervalTo(DateHelper.parseDate("2003-02-05"),
					DateHelper.parseDate("2003-02-26"));

		assertEquals(expected, result);
		
		c = new ScheduleContext(
				DateHelper.parseDate("2003-02-15"));
		
		result = test.nextDue(c);

		expected = new IntervalTo(DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-26"));
		
		assertEquals(expected, result);
		
		c = new ScheduleContext(
				DateHelper.parseDate("2003-02-15"));
		
		result = test.nextDue(c);

		expected = new IntervalTo(DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-26"));
		
		assertEquals(expected, result);
		
		c = new ScheduleContext(
				DateHelper.parseDate("2003-02-27"));
		result = test.nextDue(c);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2004-02-05"),
				DateHelper.parseDate("2004-02-26"));
		
		assertEquals(expected, result);
	}


   @Test
	public void testOverYearBoundary() throws ParseException {
		YearlySchedule s = new YearlySchedule();
		s.setFromDate("12-17");
		s.setToDate("01-04");
		
		ScheduleContext c = new ScheduleContext(
				DateHelper.parseDate("2011-01-02"));
		Interval result = s.nextDue(c);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2010-12-17"),
				DateHelper.parseDate("2011-01-05"));
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testOn() throws ParseException {
		
		YearlySchedule test = new YearlySchedule();
		test.setOnDate("05-26");
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDate("2011-11-17"));
		
		ScheduleResult result = test.nextDue(context);
		
		ScheduleResult  expected = new IntervalTo(
				DateHelper.parseDate("2012-05-26"),
				DateHelper.parseDate("2012-05-27"));
		
		assertEquals(expected, result);
		
		context = context.move(result.getUseNext());
		
		result = test.nextDue(context);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2013-05-26"),
				DateHelper.parseDate("2013-05-27"));
		
		assertEquals(expected, result);
		
	}
	
   @Test
	public void test29thFeb() throws ParseException {
		
		YearlySchedule test = new YearlySchedule();
		test.setOnDate("02-29");
		
		ScheduleContext c = new ScheduleContext(
				DateHelper.parseDate("2008-01-01"));
		Interval result = test.nextDue(c);

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2008-02-29"),
				DateHelper.parseDate("2008-03-01"));
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testFromToExample() throws ArooaParseException, ParseException {
		
		OddjobDescriptorFactory df = new OddjobDescriptorFactory();

		ArooaDescriptor descriptor = df.createDescriptor(
				getClass().getClassLoader());

		StandardFragmentParser parser = new StandardFragmentParser(descriptor);

		parser.parse(new XMLConfiguration(
				"org/oddjob/schedules/schedules/DayOfYearFromTo.xml", 
				getClass().getClassLoader()));

		Schedule schedule = (Schedule)	parser.getRoot();

		Interval next = schedule.nextDue(new ScheduleContext(
				DateHelper.parseDate("2010-02-15")));

		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2010-12-25 00:00"),
				DateHelper.parseDateTime("2010-12-27 00:00"));
		
		assertEquals(expected, next);
	}
	
   @Test
	public void testOnExample() throws ArooaParseException, ParseException {
		
		OddjobDescriptorFactory df = new OddjobDescriptorFactory();

		ArooaDescriptor descriptor = df.createDescriptor(
				getClass().getClassLoader());

		StandardFragmentParser parser = new StandardFragmentParser(descriptor);

		parser.parse(new XMLConfiguration(
				"org/oddjob/schedules/schedules/DayOfYearOn.xml", 
				getClass().getClassLoader()));

		Schedule schedule = (Schedule)	parser.getRoot();

		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDate("2010-02-15"));
		
		ScheduleResult next = schedule.nextDue(context);

		ScheduleResult expected = new IntervalTo(
				DateHelper.parseDateTime("2011-01-01 00:00"),
				DateHelper.parseDateTime("2011-01-02 00:00"));
		
		assertEquals(expected, next);
		
		context = context.move(next.getUseNext());
		
		next = schedule.nextDue(context);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2012-01-01 00:00"),
				DateHelper.parseDateTime("2012-01-02 00:00"));
		
		assertEquals(expected, next);
	}
}
