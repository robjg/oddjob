/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
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
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;

/**
 * 
 */
public class BrokenScheduleTest extends TestCase {
	
	static Schedule brokenSchedule() throws ParseException, NoConversionAvailableException, ConversionFailedException {
		
		WeeklySchedule d1 = new WeeklySchedule();
		d1.setOn(DayOfWeek.Days.MONDAY);
		WeeklySchedule d2 = new WeeklySchedule();
		d2.setOn(DayOfWeek.Days.WEDNESDAY);

		ScheduleList s1 = new ScheduleList();
		s1.setSchedules(new Schedule[] { d1, d2 });
		
		YearlySchedule s2 = new YearlySchedule();
		s2.setFromDate("02-15");
		s2.setToDate("02-25");
		
		BrokenSchedule b = new BrokenSchedule();
		b.setSchedule(s1);
		b.setBreaks(s2);
		
		return b;
	}
	
	// Feb 2003
	// Mo  We
	// 3   5
	// 10  12
	// 17  19
	// 24  26
	// 31
	
	public void testFromStartBeforeBreaks() throws Exception {
		Schedule s = brokenSchedule();
		
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(s);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2003-02-01"));

		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-03"),
				DateHelper.parseDate("2003-02-04"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-05"),
				DateHelper.parseDate("2003-02-06"));
		
		assertEquals(expected, results[1]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-10"),
				DateHelper.parseDate("2003-02-11"));
		
		assertEquals(expected, results[2]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-12"),
				DateHelper.parseDate("2003-02-13"));
		
		assertEquals(expected, results[3]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-26"),
				DateHelper.parseDate("2003-02-27"));
		
		assertEquals(expected, results[4]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-03-03"),
				DateHelper.parseDate("2003-03-04"));
		
		assertEquals(expected, results[5]);

	}
	
	public void testFromStartOnBreak() throws Exception {
		Schedule s = brokenSchedule();
				
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(s);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2003-02-15"));

		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-26"),
				DateHelper.parseDate("2003-02-27"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-03-03"),
				DateHelper.parseDate("2003-03-04"));
		
		assertEquals(expected, results[1]);

	}

	public void testStartAfterBreaks() throws Exception {
		Schedule s = brokenSchedule();
		
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(s);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2003-02-26"));

		expected = new IntervalTo(
				DateHelper.parseDate("2003-02-26"),
				DateHelper.parseDate("2003-02-27"));
		
		assertEquals(expected, results[0]);
		
		expected = new IntervalTo(
				DateHelper.parseDate("2003-03-03"),
				DateHelper.parseDate("2003-03-04"));
		
		assertEquals(expected, results[1]);

	}
	
	/** 
	 * If a schedule spans the break, the break is ignored.
	 * (because the schedule start before the break).
	 * 
	 * @throws ParseException
	 */
	public void testScheduleSpansBreaks() throws ParseException {
		
		MonthlySchedule schedule = new MonthlySchedule();
		
		MonthlySchedule breaks = new MonthlySchedule();
		breaks.setOnDay(new DayOfMonth.Number(13));
		
		BrokenSchedule test = new BrokenSchedule();
		test.setBreaks(breaks);
		test.setSchedule(schedule);
		
		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDate("2011-09-10"));
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDate("2011-09-01"),
				DateHelper.parseDate("2011-10-01"));
		
		assertEquals(expected, results[0]);
		
	}
	
	/**
	 * Check the time isn't moved.
	 * 
	 * @throws ParseException
	 */
	public void testWithTimeThatIsMaskedByBreak() throws ParseException {
		
		DateSchedule date = new DateSchedule();
		date.setOn("2010-05-03");

		TimeSchedule time = new TimeSchedule();
		time.setAt("07:00");
		
		BrokenSchedule broken = new BrokenSchedule();
		broken.setSchedule(time);
		broken.setBreaks(date);
		
		ScheduleContext context = new ScheduleContext(
				DateHelper.parseDateTime("2010-05-02 20:00"));
		
		ScheduleResult result = broken.nextDue(context);
		
		assertEquals(null, result);		
	}
	
	/**
	 * Test when a time overlaps into the break.
	 * 
	 * @throws ParseException
	 */
	public void testOverlappingSchedule() throws ParseException {
		
		WeeklySchedule dayOfWeekSchedule = new WeeklySchedule();
		dayOfWeekSchedule.setOn(DayOfWeek.Days.THURSDAY);
		
		DailySchedule time = new DailySchedule();
		time.setFrom("22:00");
		time.setTo("02:00");

		dayOfWeekSchedule.setRefinement(time);
		
		DateSchedule aBreak = new DateSchedule();
		aBreak.setFrom("2009-02-27"); // A Friday
		aBreak.setTo("2009-03-09"); // A week Monday

		BrokenSchedule test = new BrokenSchedule();
		
		test.setSchedule(dayOfWeekSchedule);
		test.setBreaks(aBreak);
		
		IntervalTo expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-26 22:00"),
				DateHelper.parseDateTime("2009-02-27 02:00"));
		
		ScheduleContext scheduleContext = new ScheduleContext(
				DateHelper.parseDateTime("2009-02-24 12:00"));
		
		ScheduleResult result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-02-27 01:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-03-12 22:00"),
				DateHelper.parseDateTime("2009-03-13 02:00"));
		
		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-02-30 12:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-03-03 12:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);

		scheduleContext = scheduleContext.move(
				DateHelper.parseDateTime("2009-03-11 00:00"));
	
		result = test.nextDue(scheduleContext);
		
		assertEquals(expected, result);
	}
	
	/**
	 * The broken schedule doesn't let the interval schedules finish.
	 * This probably isn't what we want.
	 * 
	 * @throws ParseException
	 */
	public void testOverlappingIntervalSchedule() throws ParseException {
		
		WeeklySchedule dayOfWeekSchedule = new WeeklySchedule();
		dayOfWeekSchedule.setOn(DayOfWeek.Days.THURSDAY);
		
		DailySchedule time = new DailySchedule();
		time.setFrom("22:00");
		time.setTo("02:00");

		IntervalSchedule interval = new IntervalSchedule();
		interval.setInterval("01:00");
		
		dayOfWeekSchedule.setRefinement(time);
		time.setRefinement(interval);
		
		DateSchedule aBreak = new DateSchedule();
		aBreak.setFrom("2009-02-27");
		aBreak.setTo("2009-03-09");

		BrokenSchedule test = new BrokenSchedule();
		
		test.setSchedule(dayOfWeekSchedule);
		test.setBreaks(aBreak);
		
		IntervalTo expected;
		
		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2009-02-26 23:30"));
		
		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-02-26 23:00"),
				DateHelper.parseDateTime("2009-02-27 00:00"));
		
		assertEquals(expected, results[0]);

		expected = new IntervalTo(
				DateHelper.parseDateTime("2009-03-12 22:00"),
				DateHelper.parseDateTime("2009-03-12 23:00"));
		
		assertEquals(expected, results[1]);
	}
	
	public void testAlternateScheduleInternals() throws ParseException {
		
		final List<Interval> parentIntervals = new ArrayList<Interval>();
				
		class Alt implements Schedule {
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				parentIntervals.add(context.getParentInterval());
				
				DailySchedule daily = new DailySchedule();
				daily.setAt("08:00");
				return daily.nextDue(context);
			}
		}
		
		Schedule schedule = new DailySchedule();
		
		DateSchedule holiday1 = new DateSchedule();
		holiday1.setOn("2012-04-06");
		
		DateSchedule holiday2 = new DateSchedule();
		holiday2.setOn("2012-04-08");
		
		ScheduleList holidayList = new ScheduleList();
		
		holidayList.setSchedules(new Schedule[] {
				holiday1, holiday2
		});

		BrokenSchedule test = new BrokenSchedule();
		test.setSchedule(schedule);
		test.setBreaks(holidayList);
		test.setAlternative(new Alt());
				
		ScheduleResult expected;
		
		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2012-04-05 20:00"));
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-05 00:00"),
					DateHelper.parseDateTime("2012-04-06 00:00")));
		
		assertEquals(expected, results[0]);

		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-06 08:00")),
				DateHelper.parseDateTime("2012-04-07 00:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-07 00:00"),
					DateHelper.parseDateTime("2012-04-08 00:00")));
		
		assertEquals(expected, results[2]);
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-08 08:00")),
				DateHelper.parseDateTime("2012-04-09 00:00"));
		
		assertEquals(expected, results[3]);
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-09 00:00"),
					DateHelper.parseDateTime("2012-04-10 00:00")));
		
		assertEquals(expected, results[4]);
		
		assertEquals(2, parentIntervals.size());

		assertEquals(new SimpleInterval(
					DateHelper.parseDateTime("2012-04-06 00:00"),
					DateHelper.parseDateTime("2012-04-07 00:00")),
				parentIntervals.get(0));
		
		assertEquals(new SimpleInterval(
				DateHelper.parseDateTime("2012-04-08 00:00"),
				DateHelper.parseDateTime("2012-04-09 00:00")),
			parentIntervals.get(1));
		
	}
	
	public void testAlternateScheduleOverWeekend() throws ParseException {
		
		final List<Interval> parentIntervals = new ArrayList<Interval>();
				
		class Alt implements Schedule {
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				parentIntervals.add(context.getParentInterval());
				
				DailySchedule daily = new DailySchedule();
				daily.setAt("08:00");
				return daily.nextDue(context);
			}
		}
		
		Schedule dailySchedule = new DailySchedule();
		
		WeeklySchedule schedule = new WeeklySchedule();
		schedule.setFrom(DayOfWeek.Days.MONDAY);
		schedule.setTo(DayOfWeek.Days.FRIDAY);
		schedule.setRefinement(dailySchedule);

		
		DateSchedule goodFriday = new DateSchedule();
		goodFriday.setOn("2012-04-06");
		
		DateSchedule easterMonday = new DateSchedule();
		easterMonday.setOn("2012-04-09");
		
		ScheduleList holidayList = new ScheduleList();
		
		holidayList.setSchedules(new Schedule[] {
				goodFriday, easterMonday
		});

		BrokenSchedule test = new BrokenSchedule();
		test.setSchedule(schedule);
		test.setBreaks(holidayList);
		test.setAlternative(new Alt());
				
		ScheduleResult expected;
		
		ScheduleRoller roller = new ScheduleRoller(test);
		
		Interval[] results = roller.resultsFrom(
				DateHelper.parseDateTime("2012-04-05 20:00"));
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-05 00:00"),
					DateHelper.parseDateTime("2012-04-06 00:00")));
		
		assertEquals(expected, results[0]);

		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-06 08:00")),
				DateHelper.parseDateTime("2012-04-07 00:00"));
		
		assertEquals(expected, results[1]);
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-09 08:00")),
				DateHelper.parseDateTime("2012-04-10 00:00"));
		
		assertEquals(expected, results[2]);
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-10 00:00"),
					DateHelper.parseDateTime("2012-04-11 00:00")));
		
		assertEquals(expected, results[3]);
		
		expected = new SimpleScheduleResult(
				new SimpleInterval(
					DateHelper.parseDateTime("2012-04-11 00:00"),
					DateHelper.parseDateTime("2012-04-12 00:00")));
		
		assertEquals(expected, results[4]);
		
		assertEquals(2, parentIntervals.size());

		assertEquals(new SimpleInterval(
					DateHelper.parseDateTime("2012-04-06 00:00"),
					DateHelper.parseDateTime("2012-04-07 00:00")),
				parentIntervals.get(0));
		
		assertEquals(new SimpleInterval(
				DateHelper.parseDateTime("2012-04-09 00:00"),
				DateHelper.parseDateTime("2012-04-10 00:00")),
			parentIntervals.get(1));
		
	}
	
    public void testBrokenScheduleExample() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/BrokenScheduleExample.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleResult next = schedule.nextDue(new ScheduleContext(
    			DateHelper.parseDateTime("2011-12-24 11:00")));
    	
    	IntervalTo expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-12-27 10:00"));
    	
    	assertEquals(expected, next);
    }
    
    public void testBrokenScheduleAlternative() throws ArooaParseException, ParseException {
    	
    	OddjobDescriptorFactory df = new OddjobDescriptorFactory();
    	
    	ArooaDescriptor descriptor = df.createDescriptor(
    			getClass().getClassLoader());
    	
    	StandardFragmentParser parser = new StandardFragmentParser(descriptor);
    	
    	parser.parse(new XMLConfiguration(
    			"org/oddjob/schedules/schedules/BrokenScheduleAlternative.xml", 
    			getClass().getClassLoader()));
    	
    	Schedule schedule = (Schedule)	parser.getRoot();
    	
    	ScheduleResult[] results = new ScheduleRoller(schedule).resultsFrom(
    			DateHelper.parseDateTime("2011-12-23 11:00"));

    	ScheduleResult expected;
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-12-23 10:00"),
    			DateHelper.parseDateTime("2011-12-24 00:00"));
    	
    	assertEquals(expected, results[0]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-12-24 11:00"),
    			DateHelper.parseDateTime("2011-12-28 00:00"));
    	
    	assertEquals(expected, results[1]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-12-28 10:00"),
    			DateHelper.parseDateTime("2011-12-29 00:00"));
    	
    	assertEquals(expected, results[2]);
    	
    	expected = new IntervalTo(
    			DateHelper.parseDateTime("2011-12-29 10:00"),
    			DateHelper.parseDateTime("2011-12-30 00:00"));
    	
    	assertEquals(expected, results[3]);
    }
}
