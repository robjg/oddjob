package org.oddjob.schedules.regression;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * A single schedule run.
 */
public class TestScheduleRun {

    private static final Logger logger = LoggerFactory.getLogger(
            TestScheduleRun.class);

    /**
     * Date running for.
     */
    private String testDate;

    private String expectedFrom;

    private String expectedTo;

    public void setExpectedFrom(String from) {
        this.expectedFrom = from;
    }

    public String getExpectedFrom() {
        return expectedFrom;
    }

    public void setExpectedTo(String to) {
        this.expectedTo = to;
    }

    public String getExpectedTo() {
        return expectedTo;
    }

    IntervalTo getExpected() {
        if (this.expectedFrom == null && this.expectedTo == null) {
            return null;
        }

        if (this.expectedTo == null) {
            return new IntervalTo(
                    DateHelper.parseDateTime(this.expectedFrom));
        } else {
            return new IntervalTo(
                    DateHelper.parseDateTime(this.expectedFrom),
                    DateHelper.parseDateTime(this.expectedTo));
        }
    }

    public void setDate(String date) {
        this.testDate = date;
    }

    public String getDate() {
        return testDate;
    }

    public void testSchedule(Schedule schedule) throws Exception {
        Date date =  DateHelper.parseDateTime(testDate);

//		DateFormat format = new SimpleDateFormat("dd-MMM-yy HH:mm:ss:SSS");
        Interval nextDue = schedule.nextDue(
                new ScheduleContext(date));

        IntervalTo expected = getExpected();

        logger.info("Given [" + testDate
                + "]: next due " + nextDue + ", expected - " + expected);

        if (expected == null) {
            return;
        }

        if (!expected.getFromDate().equals(nextDue.getFromDate())
                || !expected.getToDate().equals(nextDue.getToDate())) {
            throw new Exception("For date: " + date + ", Expected: " + expected + ", Actual: " + nextDue);
        }
    }
}
