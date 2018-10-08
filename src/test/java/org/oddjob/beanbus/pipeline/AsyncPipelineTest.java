package org.oddjob.beanbus.pipeline;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Ignore
public class AsyncPipelineTest {

    @Test
    public void whenStartJustConnectedToEndThenWorks() {

        Pipeline<String> test = new AsyncPipeline2(Runnable::run);

        Processor<String, List<String>> start =
                test.to(Captures.toList())
                        .create();

        start.accept("Apple");

        List<String> results = start.complete();

        assertThat(results, is(Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    private static class IdentitySection<T> implements FlushableConsumer<T> {

        private final FlushableConsumer<T> next;

        IdentitySection(FlushableConsumer<T> next) {
            this.next = next;
        }

        @Override
        public void accept(T data) {
            next.accept(data);
        }

        @Override
        public void flush() {
            next.flush();
        }
    }

    @Test
    public void registeredComponentWorks() {

        Pipeline<String> test = AsyncPipeline2.start(Runnable::run);

        WireTap<String> results = new WireTap<>();

        FlushableConsumer<String> section = test.createSection(new IdentitySection<>(results));

        FlushableConsumer<String> start = test.createSection(section);

        start.accept("Apple");

        assertThat(results.toCollection(), is( Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Test
    @Ignore
    public void flushBlocks() {

        List<Runnable> work = new ArrayList<>();

        Pipeline<String> test = null; // new AsyncPipeline(work::add, 1);

        WireTap<String> results = new WireTap<>();

        FlushableConsumer<String> section = test.createSection(new IdentitySection<>(results));

        FlushableConsumer<String> start = test.openWith(section);

        start.accept("Apple");

        try {
            start.complete();
            fail("Should timeout");
        } catch (IllegalStateException e) {
            // expected.
        }

        assertThat(work.size(), is(1));

        work.forEach(Runnable::run);

        List<String> results = start.complete();

        assertThat(results.toCollection(), is( Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Ignore
    @Test
    public void workBlocks() {

        List<Runnable> work = new ArrayList<>();

        Pipeline<String> test = null; // new AsyncPipeline(work::add, 1);

        WireTap<String> results = new WireTap<>();

        FlushableConsumer<String> section = test.createBlockSection(results, 0);

        FlushableConsumer<String> start = test.openWith(section);

        processor.accept("Apple");

        try {
            processor.accept("Pear");
            fail("Should timeout");
        } catch (IllegalStateException e) {
            // expected.
        }

        assertThat(work.size(), is(1));

        work.forEach(Runnable::run);

        processor.accept("Pear");

        work.forEach(Runnable::run);

        List<String> results = processor.complete();

        assertThat(results, is(Arrays.asList("Apple", "Pear").stream().collect(Collectors.toList())));
    }
}