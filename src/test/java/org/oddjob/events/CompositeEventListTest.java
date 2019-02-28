package org.oddjob.events;

import org.junit.Test;
import org.oddjob.arooa.utils.DateHelper;

import java.text.ParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CompositeEventListTest {

    @Test
    public void createSimpleTwoEventComposite() throws ParseException {

        EventOf<String> event1 = new WrapperOf<>(
                "apple",
                DateHelper.parseDateTime("2019-02-15 07:00"));

        EventOf<String> event2 = new WrapperOf<>(
                "pear",
                DateHelper.parseDateTime("2019-02-15 06:30"));

        CompositeEventList<String> test =
                new CompositeEventList<>(event1, event2);

        assertThat(test.getOf(), is("apple"));
        assertThat(test.getTime(),
                   is(DateHelper.parseDateTime("2019-02-15 07:00")
                                .toInstant()));
        assertThat(test.getEvents(0), is(event1));
        assertThat(test.getEvents(1), is(event2));
    }

    @Test
    public void createCompositeOfComposite() throws ParseException {

        EventOf<String> event1 = new WrapperOf<>(
                "apple",
                DateHelper.parseDateTime("2019-02-15 07:00"));

        EventOf<String> event2 = new WrapperOf<>(
                "pear",
                DateHelper.parseDateTime("2019-02-15 06:30"));

        CompositeEventList<String> test =
                new CompositeEventList<>(
                        new CompositeEventList<>(event1),
                        new CompositeEventList<>(event2));

        assertThat(test.getOf(), is("apple"));
        assertThat(test.getTime(),
                   is(DateHelper.parseDateTime("2019-02-15 07:00")
                                .toInstant()));

        assertThat(((CompositeEventList<String>)test.getEvents(0))
                           .getEvents(0), is(event1));
        assertThat(((CompositeEventList<String>)test.getEvents(1))
                           .getEvents(0), is(event2));
    }

}