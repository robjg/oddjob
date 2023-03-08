package org.oddjob.schedules;

import org.junit.Test;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.schedules.DailySchedule;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.tools.ManualClock;
import org.oddjob.util.Clock;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScheduleCalculatorX2Test {

    static class OurClock implements Clock {

        boolean boobyTrapped;


        public Date getDate() {
            if (boobyTrapped) {
                throw new RuntimeException();
            } else {
                return DateHelper.parseDateTime("2009-02-15 13:51");
            }
        }
    }

    static class Results implements ScheduleListener {

        Date scheduleDate;
        Date retryDate;
        boolean failed;

        @Override
        public void complete(Date scheduleDate, ScheduleResult lastComplete) {
            throw new RuntimeException("Unexpected.");
        }

        @Override
        public void failed(Date scheduleDate) {
            this.scheduleDate = scheduleDate;
            retryDate = null;
            failed = true;
        }

        @Override
        public void initialised(Date scheduleDate) {
            this.scheduleDate = scheduleDate;
        }

        @Override
        public void retry(Date scheduleDate, Date retryDate) {
            if (!scheduleDate.equals(this.scheduleDate)) {
                throw new RuntimeException();
            }
            this.retryDate = retryDate;
        }
    }

    @Test
    public void testUserGuideScheduleExample() throws ParseException {

        DailySchedule schedule = new DailySchedule();
        schedule.setFrom("07:00");

        DailySchedule retry = new DailySchedule();
        retry.setFrom("07:00");
        retry.setTo("14:00");

        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:15");

        retry.setRefinement(interval);

        OurClock clock = new OurClock();

        ScheduleCalculator test = new ScheduleCalculator(clock, schedule, retry);

        Results results = new Results();

        test.addScheduleListener(results);

        test.initialise(null, new HashMap<>());

        assertEquals(DateHelper.parseDateTime("2009-02-15 07:00"),
                results.scheduleDate);

        test.calculateRetry();

        assertEquals(DateHelper.parseDateTime("2009-02-15 14:00"),
                results.retryDate);

        // don't use clock again.
        clock.boobyTrapped = true;

        test.calculateRetry();

        assertTrue(results.failed);
        assertEquals(DateHelper.parseDateTime("2009-02-16 07:00"),
                results.scheduleDate);
    }

    @Test
    public void testUserGuideScheduleExampleLateStart() throws ParseException {

        DailySchedule schedule = new DailySchedule();
        schedule.setFrom("07:00");

        DailySchedule retry = new DailySchedule();
        retry.setFrom("07:00");
        retry.setTo("14:00");

        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:15");

        retry.setRefinement(interval);

        Clock clock = new ManualClock("2009-02-15 20:00");

        ScheduleCalculator test = new ScheduleCalculator(clock, schedule, retry);

        Results results = new Results();

        test.addScheduleListener(results);

        test.initialise(null, new HashMap<>());

        assertEquals(DateHelper.parseDateTime("2009-02-15 07:00"),
                results.scheduleDate);

        test.calculateRetry();

        assertTrue(results.failed);
        assertEquals(DateHelper.parseDateTime("2009-02-16 07:00"),
                results.scheduleDate);
    }

    static class Results2 implements ScheduleListener {

        Date scheduleDate;

        @Override
        public void complete(Date scheduleDate, ScheduleResult lastComplete) {
            this.scheduleDate = scheduleDate;
        }

        @Override
        public void failed(Date scheduleDate) {
            throw new RuntimeException("Unexpected.");
        }

        @Override
        public void initialised(Date scheduleDate) {
            this.scheduleDate = scheduleDate;
        }

        @Override
        public void retry(Date scheduleDate, Date retryDate) {
            throw new RuntimeException("Unexpected.");
        }
    }

    @Test
    public void testClockNotUsedAfterInitialise() throws ParseException {

        IntervalSchedule interval = new IntervalSchedule();
        interval.setInterval("00:15");

        OurClock clock = new OurClock();

        ScheduleCalculator test = new ScheduleCalculator(clock, interval, (Schedule) null);

        Results2 results = new Results2();

        test.addScheduleListener(results);

        test.initialise(null, new HashMap<>());

        assertEquals(DateHelper.parseDateTime("2009-02-15 13:51"),
                results.scheduleDate);

        // don't use clock again.
        clock.boobyTrapped = true;

        test.calculateComplete();

        assertEquals(DateHelper.parseDateTime("2009-02-15 14:06"),
                results.scheduleDate);

        test.calculateComplete();

        assertEquals(DateHelper.parseDateTime("2009-02-15 14:21"),
                results.scheduleDate);

    }
}
