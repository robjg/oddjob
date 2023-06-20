package org.oddjob.state;


import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StateInstantClockTest {

    @Test
    void whenInstantTheSame() {

        List<Long> nanos = List.of(10L, 20L, 30L, 40L);
        Iterator<Long> it = nanos.iterator();

        StateInstantClock test = StateInstantClock.fromClock(
                Clock.fixed(Instant.ofEpochSecond(100L), ZoneOffset.UTC),
                it::next);

        Instant one = test.now();
        Instant two = test.now();
        Instant three = test.now();

        assertThat(one.toString(), is("1970-01-01T00:01:40.000000010Z"));
        assertThat(two.toString(), is("1970-01-01T00:01:40.000000020Z"));
        assertThat(three.toString(), is("1970-01-01T00:01:40.000000030Z"));
    }


    @Test
    void whenInstantAndNanosTheSame() {

        StateInstantClock test = StateInstantClock.fromClock(
                Clock.fixed(Instant.ofEpochSecond(100L), ZoneOffset.UTC),
                () -> 0L);

        Instant one = test.now();
        Instant two = test.now();
        Instant three = test.now();

        assertThat(one.toString(), is("1970-01-01T00:01:40.000000001Z"));
        assertThat(two.toString(), is("1970-01-01T00:01:40.000000002Z"));
        assertThat(three.toString(), is("1970-01-01T00:01:40.000000003Z"));
    }

}