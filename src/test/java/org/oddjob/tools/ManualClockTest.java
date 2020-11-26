package org.oddjob.tools;


import org.junit.Test;
import org.oddjob.arooa.utils.DateHelper;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ManualClockTest {

    @Test
    public void testFromDate() throws ParseException {

        Date date = DateHelper.parseDate("2020-11-20 06:55");

        ManualClock manualClock = new ManualClock();
        manualClock.setDate(date);

        assertThat(manualClock.getDate(), is(date));
        assertThat(manualClock.instant(), is(date.toInstant()));
        assertThat(manualClock.getZone(), is(ZoneId.systemDefault()));
        assertThat(manualClock.getInstant(), is(date.toInstant()));
    }

    @Test
    public void testFromInstant() throws ParseException {

        Date date = DateHelper.parseDate("2020-11-20 06:55");

        ManualClock manualClock = new ManualClock();
        manualClock.setInstant(date.toInstant());

        assertThat(manualClock.getDate(), is(date));
        assertThat(manualClock.instant(), is(date.toInstant()));
        assertThat(manualClock.getZone(), is(ZoneId.systemDefault()));
        assertThat(manualClock.getInstant(), is(date.toInstant()));
    }

    @Test
    public void testFromBuilder() throws ParseException {

        Date date = DateHelper.parseDate("2020-11-20 06:55");

        ManualClock manualClock = ManualClock.fromInstant(date.toInstant())
                .andSystemZone();

        assertThat(manualClock.getDate(), is(date));
        assertThat(manualClock.instant(), is(date.toInstant()));
        assertThat(manualClock.getZone(), is(ZoneId.systemDefault()));
        assertThat(manualClock.getInstant(), is(date.toInstant()));
    }
}