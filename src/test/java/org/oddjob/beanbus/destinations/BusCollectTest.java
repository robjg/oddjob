package org.oddjob.beanbus.destinations;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.ConversionPath;
import org.oddjob.arooa.convert.DefaultConversionRegistry;
import org.oddjob.arooa.convert.DefaultConverter;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BusCollectTest {

    @Test
    void whenDefaultsThenCollectToList() throws FailedToStopException {

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
    void whenKeyMapperThenCollectToMap() throws FailedToStopException {

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
    void whenKeyMapperAndValueMapperThenCollectToMap() throws FailedToStopException {

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
    void whenOutputThenCollectLines() throws FailedToStopException {

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

    @Test
    void whenListContainerToStringTheAsExpected() throws FailedToStopException {

        BusCollect<Integer> collect = new BusCollect<>();

        assertThat(collect.getList().toString(), is("[]"));

        collect.start();

        collect.accept(1);

        assertThat(collect.getList().toString(), is("[1]"));

        collect.accept(2);

        assertThat(collect.getList().toString(), is("[1, 2]"));

        collect.accept(3);
        collect.accept(4);
        collect.accept(5);

        assertThat(collect.getList().toString(), is("[1, 2, 3, 4, 5]"));
        assertThat(collect.getList().toString(), is("[1, 2, 3, 4, 5]"));

        collect.accept(6);

        assertThat(collect.getList().toString(), is("[1, 2, 3, 4, 5, ...]"));
        assertThat(collect.getList().toString(), is("[1, 2, 3, 4, 5, ...]"));

        collect.accept(7);

        assertThat(collect.getList().toString(), is("[1, 2, 3, 4, 5, ...]"));

        collect.stop();
    }

    @Test
    void whenMapContainerToStringTheAsExpected() throws FailedToStopException {

        BusCollect<Integer> collect = new BusCollect<>();

        assertThat(collect.getMap().toString(), is("{}"));

        collect.setKeyMapper(Function.identity());
        collect.setValueMapper(Function.identity());

        collect.start();

        collect.accept(1);

        assertThat(collect.getMap().toString(), is("{1=1}"));

        collect.accept(2);

        assertThat(collect.getMap().toString(), is("{1=1, 2=2}"));

        collect.accept(3);
        collect.accept(4);
        collect.accept(5);

        assertThat(collect.getMap().toString(), is("{1=1, 2=2, 3=3, 4=4, 5=5}"));
        assertThat(collect.getMap().toString(), is("{1=1, 2=2, 3=3, 4=4, 5=5}"));

        collect.accept(6);

        assertThat(collect.getMap().toString(), is("{1=1, 2=2, 3=3, 4=4, 5=5, ...}"));
        assertThat(collect.getMap().toString(), is("{1=1, 2=2, 3=3, 4=4, 5=5, ...}"));

        collect.accept(7);

        assertThat(collect.getMap().toString(), is("{1=1, 2=2, 3=3, 4=4, 5=5, ...}"));

        collect.stop();
    }

    @Test
    void conversions() throws ConversionFailedException {

        DefaultConversionRegistry conversionRegistry = new DefaultConversionRegistry();

        new BusCollect.Conversions().registerWith(conversionRegistry);

        BusCollect<Integer> test = new BusCollect<>();
        test.start();
        test.accept(1);
        test.accept(2);
        test.accept(3);

        ConversionPath<BusCollect.ListContainer, Iterable> path =
                conversionRegistry.findConversion(BusCollect.ListContainer.class, Iterable.class);

        assertThat(path, Matchers.notNullValue());

        Iterable<?> iterable = path.convert(test.getList(), new DefaultConverter());

        List<?> results = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());

        assertThat(results, contains(1, 2, 3));
    }


}