package org.oddjob.events;

import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EventConversionsTest {

    @Test
    public void testToListWithNestedComposites() {

        Instants instants = new Instants();

        CompositeEvent<Integer> compositeEvent =
                new CompositeEventList<Integer>(
                        new WrapperOf<>(1, instants.next()),
                        new CompositeEventList<>(
                                new WrapperOf<>(2, instants.next()),
                                new WrapperOf<>(3, instants.next())
                        ));

        List<Integer> results = EventConversions.toList(compositeEvent);

        assertThat(results, is(Arrays.asList(1, 2, 3)));

        assertThat(compositeEvent.getOfs(), is(results));
    }

    static class Instants {

        private final AtomicLong time = new AtomicLong(
                Instant.parse("2019-02-26T20:00:00Z").toEpochMilli());

        public Instant next() {
            return Instant.ofEpochMilli(time.getAndAdd(1000L));
        }
    }
}