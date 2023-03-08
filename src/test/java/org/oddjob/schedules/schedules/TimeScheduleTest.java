/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules.schedules;

import org.junit.Before;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.*;
import org.oddjob.schedules.units.DayOfMonth;
import org.oddjob.schedules.units.DayOfWeek;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/**
 *
 */
public class TimeScheduleTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger("org.oddjob");

    @Before
    public void setUp() {
        logger.debug("============== " + getName() + " ==================");
    }

    @Test
    public void testStandardIntervalDifferentStarts() {

        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");
        test.setTo("11:00");

        ScheduleResult result, expected;

        // before

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2006-03-02 09:00"));

        result = test.nextDue(context);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2006-03-02 10:00"),
                        DateHelper.parseDateTime("2006-03-02 11:00")),
                null);

        assertEquals(expected, result);

        // during

        context = new ScheduleContext(
                DateHelper.parseDateTime("2006-03-02 10:30"));

        result = test.nextDue(context);

        assertEquals(expected, result);

        // after

        context = new ScheduleContext(
                DateHelper.parseDateTime("2006-03-02 11:30"));

        result = test.nextDue(context);

        expected = null;

        assertEquals(expected, result);
    }

    @Test
    public void testForwardInterval() {

        TimeSchedule s = new TimeSchedule();
        s.setFrom("11:00");
        s.setTo("10:00");

        ScheduleResult expected, result;

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2006-03-02 11:00"),
                        DateHelper.parseDateTime("2006-03-03 10:00")),
                null);

        result = s.nextDue(new ScheduleContext(
                DateHelper.parseDateTime("2006-03-02 09:00")));

        assertEquals(expected, result);

        result = s.nextDue(new ScheduleContext(
                DateHelper.parseDateTime("2006-03-02 10:00")));

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2006-03-02 11:00"),
                        DateHelper.parseDateTime("2006-03-03 10:00")),
                null);

        assertEquals(expected, result);

        result = s.nextDue(new ScheduleContext(
                DateHelper.parseDateTime("2006-03-02 11:30")));

        assertEquals(expected, result);
    }

    @Test
    public void testOn() {

        TimeSchedule s = new TimeSchedule();
        s.setAt("12:00");

        ScheduleContext context;

        ScheduleResult expected, result;

        context = new ScheduleContext(
                DateHelper.parseDateTime("2005-06-21 8:00"));

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2005-06-21 12:00")),
                null);

        result = s.nextDue(context);

        assertEquals(expected, result);

        context = new ScheduleContext(
                DateHelper.parseDateTime("2005-06-21 12:00"));

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2005-06-21 12:00")),
                null);

        result = s.nextDue(context);

        assertEquals(expected, result);

        context = new ScheduleContext(
                DateHelper.parseDateTime("2005-06-21 12:30"));

        expected = null;

        result = s.nextDue(context);

        assertEquals(expected, result);
    }

    @Test
    public void testWithLimits() throws ParseException {

        TimeSchedule test = new TimeSchedule();
        test.setAt("12:00");

        ScheduleContext context;

        ScheduleResult expected, result;

        context = new ScheduleContext(
                DateHelper.parseDateTime("2020-06-20 12:00"));

        context = context.spawn(new IntervalTo(
                DateHelper.parseDate("2020-06-21"),
                DateHelper.parseDate("2020-06-22")));

        result = test.nextDue(context);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2020-06-21 12:00")),
                null);

        assertEquals(expected, result);
    }

    // with just a from time
    @Test
    public void testDefaultTo() {
        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2005-12-25 09:00"));

        Interval result = test.nextDue(context);

        logger.debug("result " + result);

        Interval expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2005-12-25 10:00"),
                        Interval.END_OF_TIME),
                null);

        assertEquals(expected, result);

    }

    // with just a to time
    @Test
    public void testDefaultFrom() {

        TimeSchedule s = new TimeSchedule();
        s.setTo("10:00");

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2005-12-25 09:00"));

        Interval result = s.nextDue(context);
        logger.debug("result " + result);

        Interval expected = new SimpleScheduleResult(
                new IntervalTo(
                        Interval.START_OF_TIME,
                        DateHelper.parseDateTime("2005-12-25 10:00")),
                null);

        assertEquals(expected, result);
    }

    @Test
    public void testWithInterval() throws Exception {
        TimeSchedule timeSchedule = new TimeSchedule();
        timeSchedule.setFrom("08:00");
        timeSchedule.setTo("11:57");

        IntervalSchedule intervalSchedule = new IntervalSchedule();
        intervalSchedule.setInterval("00:15");
        timeSchedule.setRefinement(intervalSchedule);

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2006-02-23 00:07"));

        ScheduleResult result = timeSchedule.nextDue(context);

        logger.debug("result " + result);

        ScheduleResult expected = (
                new IntervalTo(
                        DateHelper.parseDateTime("2006-02-23 08:00"),
                        DateHelper.parseDateTime("2006-02-23 08:15")));

        assertEquals(expected, result);

        assert result != null;
        result = timeSchedule.nextDue(
                context.move(result.getUseNext()));

        expected =
                new IntervalTo(
                        DateHelper.parseDateTime("2006-02-23 08:15"),
                        DateHelper.parseDateTime("2006-02-23 08:30"));

        assertEquals(expected, result);

        // In the last interval.

        context = new ScheduleContext(
                DateHelper.parseDateTime("2006-02-23 11:55"));

        result = timeSchedule.nextDue(context);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2006-02-23 11:45"),
                        DateHelper.parseDateTime("2006-02-23 12:00")),
                null);

        assertEquals(expected, result);

        // Past the to date, but still in the last interval.

        context = new ScheduleContext(
                DateHelper.parseDateTime("2006-02-23 11:59"));

        result = timeSchedule.nextDue(context);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2006-02-23 11:45"),
                        DateHelper.parseDateTime("2006-02-23 12:00")),
                null);

        assertEquals(expected, result);

        // past for that day.

        context = new ScheduleContext(
                DateHelper.parseDateTime("2006-02-23 12:00:00"));

        result = timeSchedule.nextDue(context);

        assertEquals(null, result);
    }

    @Test
    public void testWithIntervalOverMidnight() throws Exception {
        TimeSchedule timeSchedule = new TimeSchedule();
        timeSchedule.setFrom("23:00");
        timeSchedule.setTo("01:50");

        IntervalSchedule intervalSchedule = new IntervalSchedule();
        intervalSchedule.setInterval("00:15");
        timeSchedule.setRefinement(intervalSchedule);

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2006-02-23 01:55"));

        ScheduleResult result = timeSchedule.nextDue(context);

        ScheduleResult expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2006-02-23 01:45"),
                        DateHelper.parseDateTime("2006-02-23 02:00")),
                null);

        assertEquals(expected, result);

    }

    @Test
    public void testNextIntervalWithParent() throws ParseException {

        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");
        test.setTo("17:00");

        // Test before

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 14:00"));

        context = context.spawn(new IntervalTo(
                DateHelper.parseDate("2003-05-26"),
                DateHelper.parseDate("2003-05-27")));

        Interval result = test.nextInterval(context);

        Interval expected = new IntervalTo(
                DateHelper.parseDateTime("2003-05-26 10:00"),
                DateHelper.parseDateTime("2003-05-26 17:00"));


        assertEquals(expected, result);

        // Test during last.

        context = context.move(
                DateHelper.parseDateTime("2003-05-25 14:00"));

        result = test.nextInterval(context);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2003-05-26 10:00"),
                DateHelper.parseDateTime("2003-05-26 17:00"));


        assertEquals(expected, result);

        // Test during current.

        context = context.move(
                DateHelper.parseDateTime("2003-05-26 14:00"));

        result = test.nextInterval(context);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2003-05-26 10:00"),
                DateHelper.parseDateTime("2003-05-26 17:00"));


        assertEquals(expected, result);

        // Test after parent interval.

        context = context.move(
                DateHelper.parseDateTime("2003-05-27 14:00"));

        result = test.nextInterval(context);

        expected = null;

        assertEquals(expected, result);
    }

    @Test
    public void testLastInterval() {

        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");
        test.setTo("17:00");

        Interval expected, result;

        // before

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 09:00"));

        result = test.lastInterval(context);

        expected = null;

        assertEquals(expected, result);

        // during

        context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 12:00"));

        result = test.lastInterval(context);

        assertEquals(expected, result);

        // after

        context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 17:00"));

        result = test.lastInterval(context);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2003-05-24 10:00"),
                DateHelper.parseDateTime("2003-05-24 17:00"));

        assertEquals(expected, result);
    }

    @Test
    public void testLastIntervalOverMidnight() {

        TimeSchedule test = new TimeSchedule();
        test.setFrom("22:00");
        test.setTo("07:00");

        Interval expected, result;

        // during

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 06:00"));

        result = test.lastInterval(context);

        expected = null;

        assertEquals(expected, result);

        // after

        context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 12:00"));

        result = test.lastInterval(context);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2003-05-23 22:00"),
                DateHelper.parseDateTime("2003-05-24 07:00"));


        assertEquals(expected, result);

        // during

        context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 23:00"));

        result = test.lastInterval(context);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2003-05-23 22:00"),
                DateHelper.parseDateTime("2003-05-24 07:00"));

        assertEquals(expected, result);
    }

    @Test
    public void testWithParent() {

        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");
        test.setTo("17:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.MONDAY);

        weekly.setRefinement(test);

        // A Saturday.
        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 14:00"));


        ScheduleResult result = weekly.nextDue(context);

        ScheduleResult expected;

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-05-26 10:00"),
                        DateHelper.parseDateTime("2003-05-26 17:00")));

        assertEquals(expected, result);

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-06-02 10:00"),
                        DateHelper.parseDateTime("2003-06-02 17:00")));

        result = weekly.nextDue(context.move(result.getUseNext()));

        assertEquals(expected, result);
    }

    @Test
    public void testAtWithParentWeekly() {

        TimeSchedule test = new TimeSchedule();
        test.setAt("17:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.MONDAY);

        weekly.setRefinement(test);

        // A Saturday.
        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 14:00"));


        ScheduleResult result = weekly.nextDue(context);

        ScheduleResult expected;

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-05-26 17:00")));

        assertEquals(expected, result);

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-06-02 17:00")));

        result = weekly.nextDue(context.move(result.getUseNext()));

        assertEquals(expected, result);
    }

    @Test
    public void testAtWithParentMonthly() {

        TimeSchedule test = new TimeSchedule();
        test.setAt("17:00");

        MonthlySchedule monthly = new MonthlySchedule();
        monthly.setFromDay(new DayOfMonth.Number(17));
        monthly.setToDay(new DayOfMonth.Number(22));

        monthly.setRefinement(test);

        ScheduleResult result, expected;

        // before
        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 14:00"));

        result = monthly.nextDue(context);

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-06-17 17:00")));

        assertEquals(expected, result);

        // during

        context = context.move(
                DateHelper.parseDateTime("2003-06-20 14:00"));

        result = monthly.nextDue(context);

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-07-17 17:00")));


        assertEquals(expected, result);

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2003-08-17 17:00")));

        result = monthly.nextDue(context.move(result.getUseNext()));

        assertEquals(expected, result);
    }

    @Test
    public void testAsChildWithInterval() throws Exception {
        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");
        test.setTo("17:00");

        IntervalSchedule intervalSchedule = new IntervalSchedule();
        intervalSchedule.setInterval("05:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.MONDAY);

        test.setRefinement(intervalSchedule);
        weekly.setRefinement(test);

        // A Saturday.
        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-24 14:00"));


        ScheduleResult result = weekly.nextDue(context);

        ScheduleResult expected;

        expected =
                new IntervalTo(
                        DateHelper.parseDateTime("2003-05-26 10:00"),
                        DateHelper.parseDateTime("2003-05-26 15:00"));

        assertEquals(expected, result);

        context = context.move(result.getUseNext());

        result = weekly.nextDue(context);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2003-05-26 15:00"),
                        DateHelper.parseDateTime("2003-05-26 20:00")));

        assertEquals(expected, result);
    }

    @Test
    public void testAsChildOverMidnightWithInterval() throws Exception {
        TimeSchedule test = new TimeSchedule();
        test.setFrom("22:00");
        test.setTo("05:00");

        IntervalSchedule intervalSchedule = new IntervalSchedule();
        intervalSchedule.setInterval("05:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.MONDAY);

        test.setRefinement(intervalSchedule);
        weekly.setRefinement(test);

        // A Saturday.
        ScheduleResult[] results = new ScheduleRoller(weekly).resultsFrom(
                DateHelper.parseDateTime("2003-05-24 14:00"));

        ScheduleResult expected;

        expected =
                new IntervalTo(
                        DateHelper.parseDateTime("2003-05-26 22:00"),
                        DateHelper.parseDateTime("2003-05-27 03:00"));

        assertEquals(expected, results[0]);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2003-05-27 03:00"),
                        DateHelper.parseDateTime("2003-05-27 08:00")));

        assertEquals(expected, results[1]);

        expected =
                new IntervalTo(
                        DateHelper.parseDateTime("2003-06-02 22:00"),
                        DateHelper.parseDateTime("2003-06-03 03:00"));

        assertEquals(expected, results[2]);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2003-06-03 03:00"),
                        DateHelper.parseDateTime("2003-06-03 08:00")));

        assertEquals(expected, results[3]);

        // Check last interval is used.

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2003-05-27 07:00"));

        ScheduleResult result;

        result = weekly.nextDue(context);

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2003-05-27 03:00"),
                        DateHelper.parseDateTime("2003-05-27 08:00")));

        assertEquals(expected, result);
    }

    /**
     * Weird things happen with more than 24 hours in a day...
     */
    @Test
    public void testTimeAfter24() throws ParseException {

        TimeSchedule test = new TimeSchedule();
        test.setFrom("10:00");
        test.setTo("25:00");

        ScheduleContext context;
        Interval expected;
        Interval result;

        context = new ScheduleContext(
                DateHelper.parseDateTime("2009-02-27"));

        expected = new SimpleScheduleResult(
                new IntervalTo(
                        DateHelper.parseDateTime("2009-02-27 10:00"),
                        DateHelper.parseDateTime("2009-02-28 01:00")),
                null);

        result = test.nextDue(context);

        assertEquals(expected, result);

        context = new ScheduleContext(
                DateHelper.parseDate("2009-02-27 12:00"));

        result = test.nextDue(context);

        assertEquals(expected, result);

    }

    /**
     * Note that the retry is never due because it starts before the
     * schedule, and so is limited by it to being never due.
     *
     */
    @Test
    public void testTwoNestedTimes() {

        TimeSchedule schedule = new TimeSchedule();
        schedule.setFrom("07:00");

        TimeSchedule retry = new TimeSchedule();
        retry.setTo("14:00");

        schedule.setRefinement(retry);

        ScheduleRoller roller = new ScheduleRoller(schedule);

        Interval[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2009-02-15 13:51"));

        assertNull(results[0]);
    }

    @Test
    public void testLimitedTimeAndAnInterval() throws ParseException {

        TimeSchedule retry = new TimeSchedule();
        retry.setFrom("07:00");
        retry.setTo("14:00");

        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:15");

        retry.setRefinement(interval);

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2009-02-15 13:51"));

        context = context.spawn(
                new SimpleInterval(
                        DateHelper.parseDateTime("2009-02-15 07:00"),
                        DateHelper.parseDateTime("2009-02-16 00:00")));

        Interval expected;
        Interval result;

        expected = new SimpleScheduleResult(
                new SimpleInterval(
                        DateHelper.parseDateTime("2009-02-15 13:45"),
                        DateHelper.parseDateTime("2009-02-15 14:00")),
                null);

        result = retry.nextDue(context);

        assertEquals(expected, result);

        result = retry.nextDue(context.move(expected.getToDate()));

        assertNull(result);
    }

    @Test
    public void testDefaultTimesRollingForward() {

        TimeSchedule test = new TimeSchedule();

        ScheduleContext context = new ScheduleContext(
                DateHelper.parseDateTime("2011-06-30 00:00"));

        Interval result = test.nextDue(context);

        Interval expected = new SimpleScheduleResult(
                new IntervalTo(
                        Interval.START_OF_TIME,
                        Interval.END_OF_TIME),
                null);

        assertEquals(expected, result);
    }
}
