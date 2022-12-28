package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.FailedToStopException;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BusCollectTest {

    @Test
    public void whenDefaultsThenCollectToList() throws FailedToStopException {

        BusCollect<String> collect = new BusCollect<>();

        collect.start();

        assertThat(collect.getList().getList(), empty());
        assertThat(collect.getCount(), is(0));

        collect.accept("A");
        collect.accept("B");

        assertThat(collect.getList().getList(), contains("A", "B"));
        assertThat(collect.getCount(), is(2));

        assertThat(collect.getMap().getMap(), anEmptyMap());

        collect.stop();
        collect.hardReset();

        assertThat(collect.getList().getList(), empty());
        assertThat(collect.getCount(), is(0));

    }

    @Test
    public void whenKeyMapperThenCollectToMap() throws FailedToStopException {

        BusCollect<String> collect = new BusCollect<>();

        collect.setKeyMapper(s -> s.substring(0, 1));

        collect.start();

        collect.accept("A,Apple");
        collect.accept("B,Banana");

        assertThat(collect.getMap().getValue("A"), is("A,Apple"));
        assertThat(collect.getMap().getValue("B"), is("B,Banana"));
        assertThat(collect.getCount(), is(2));

        assertThat(collect.getList().getList(), empty());

        collect.stop();
        collect.hardReset();

        assertThat(collect.getMap().getMap(), anEmptyMap());
    }

    @Test
    public void whenKeyMapperAndValueMapperThenCollectToMap() throws FailedToStopException {

        BusCollect<String> collect = new BusCollect<>();

        collect.setKeyMapper(s -> s.substring(0, 1));
        collect.setValueMapper(s -> s.substring(2));

        collect.start();

        collect.accept("A,Apple");
        collect.accept("B,Banana");

        assertThat(collect.getMap().getValue("A"), is("Apple"));
        assertThat(collect.getMap().getValue("B"), is("Banana"));
        assertThat(collect.getCount(), is(2));

        collect.stop();
        collect.hardReset();

        assertThat(collect.getMap().getMap(), anEmptyMap());
    }

    @Test
    public void whenOutputThenCollectLines() throws FailedToStopException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        BusCollect<String> collect = new BusCollect<>();
        collect.setOutput(out);

        collect.start();

        collect.accept("Apple");
        collect.accept("Banana");

        collect.stop();

        assertThat(collect.getCount(), is(2));
        assertThat(new String(out.toByteArray()), is("Apple" + System.lineSeparator()
                + "Banana" + System.lineSeparator()));

        collect.hardReset();

        assertThat(collect.getMap().getMap(), anEmptyMap());
    }
}