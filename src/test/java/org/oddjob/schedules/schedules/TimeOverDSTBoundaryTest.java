package org.oddjob.schedules.schedules;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.ScheduleRoller;
import org.oddjob.schedules.units.DayOfWeek;

import java.text.ParseException;
import java.util.TimeZone;

public class TimeOverDSTBoundaryTest extends OjTestCase {

    //
    // At boundary start.

    @Test
    public void testDayLightSavingInAutumnWithAtBoundary() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setAt("01:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2005-10-28 12:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-10-30T01:00:00Z"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-11-06 01:00"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    @Test
    public void testDayLightSavingInSpringWithAtBoundary() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setAt("01:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-03-27 01:00"));


        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-04-03 01:00"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    //
    // At boundary end.

    @Test
    public void testDayLightSavingInAutumnWithAtBoundary2() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setAt("02:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2005-10-28 12:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-10-30 02:00"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-11-06 02:00"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    @Test
    public void testDayLightSavingInSpringWithAtBoundary2() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setAt("02:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-03-27 02:00"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-04-03 02:00"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    //
    // On boundary.

    @Test
    public void testDayLightSavingInAutumnWithFromToOnBoundary() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("01:00");
        test.setTo("02:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2005-10-28 12:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-10-30T01:00:00Z"),
                DateHelper.parseDateTime("2005-10-30T02:00:00Z"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-11-06 01:00"),
                DateHelper.parseDateTime("2005-11-06 02:00"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    @Test
    public void testDayLightSavingInSpringWithFromToOnBoundary() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("01:00");
        test.setTo("02:00");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-03-27 01:00"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-04-03 01:00"),
                DateHelper.parseDateTime("2005-04-03 02:00"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    //
    // Spanning boundary start

    @Test
    public void testDayLightSavingInAutumnWithFromToSpanningBoundary() {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("00:45");
        test.setTo("01:15");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2005-10-28 12:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-10-29T23:45:00Z"),
                DateHelper.parseDateTime("2005-10-30T01:15:00Z"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-11-06 00:45"),
                DateHelper.parseDateTime("2005-11-06 01:15"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    @Test
    public void testDayLightSavingInSpringWithFromToSpanningBoundary() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("00:45");
        test.setTo("01:15");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-03-27 00:45"),
                DateHelper.parseDateTime("2005-03-27 02:00"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-04-03 00:45"),
                DateHelper.parseDateTime("2005-04-03 01:15"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    //
    // Spanning boundary end

    @Test
    public void testDayLightSavingInAutumnWithFromToSpanningBoundary2() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("01:45");
        test.setTo("02:15");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2005-10-28 12:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-10-30T01:45:00Z"),
                DateHelper.parseDateTime("2005-10-30T02:15:00Z"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-11-06 01:45"),
                DateHelper.parseDateTime("2005-11-06 02:15"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    @Test
    public void testDayLightSavingInSpringWithFromToSpanningBoundary2() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("01:45");
        test.setTo("02:15");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(DateHelper.parseDate("2005-03-26 00:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-03-27 02:00"),
                DateHelper.parseDateTime("2005-03-27 02:15"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-04-03 01:45"),
                DateHelper.parseDateTime("2005-04-03 02:15"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    //
    // Over Midnight (from > to)

    @Test
    public void testDayLightSavingInAutumnOverMidnightSpanningBoundary() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("01:30");
        test.setTo("00:30");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDateTime("2005-10-28 12:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-10-30T01:30:00Z"),
                DateHelper.parseDateTime("2005-10-31T00:30:00Z"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-11-06 01:30"),
                DateHelper.parseDateTime("2005-11-07 00:30"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }

    @Test
    public void testDayLightSavingInSpringOverMidnightSpanningBoundary() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        TimeSchedule test = new TimeSchedule();
        test.setFrom("01:30");
        test.setTo("00:30");

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setOn(DayOfWeek.Days.SUNDAY);

        weekly.setRefinement(test);

        ScheduleRoller roller = new ScheduleRoller(weekly);

        ScheduleResult[] results = roller.resultsFrom(
                DateHelper.parseDate("2005-03-26 00:00"));

        ScheduleResult expected;

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-03-27 02:00"),
                DateHelper.parseDateTime("2005-03-28 00:30"));

        assertEquals(expected, results[0]);

        expected = new IntervalTo(
                DateHelper.parseDateTime("2005-04-03 01:30"),
                DateHelper.parseDateTime("2005-04-04 00:30"));

        assertEquals(expected, results[1]);

        TimeZone.setDefault(null);
    }
}
