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

    private static class IdentitySection<T> implements Section<T, T> {

        @Override
        public Pipe<T> linkTo(Consumer<? super T> next) {
            return new Pipe<T>() {
                @Override
                public void accept(T data) {
                    next.accept(data);
                }

                @Override
                public void flush() {

                }
            };
        }
    }

    @Test
    public void registeredComponentWorks() {

        Pipeline<String> test = AsyncPipeline2.start(Runnable::run);

        Pipeline.Stage<String, String> begin = test
                .to(new IdentitySection<>());

        Pipeline.Join<String, String> join = test.join();
        join.join(begin);

        Processor<String, List<String>> processor =
                join.to(Captures.toList())
                        .create();

        processor.accept("Apple");

        List<String> results = processor.complete();

        assertThat(results, is(Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Test
    @Ignore
    public void flushBlocks() {

        List<Runnable> work = new ArrayList<>();

        Pipeline<String> test = null; // new AsyncPipeline(work::add, 1);

        Processor<String, List<String>> start =
                test.to(new IdentitySection<>())
                        .to(Captures.toList())
                        .create();

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

        assertThat(results, is(Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Ignore
    @Test
    public void workBlocks() {

        List<Runnable> work = new ArrayList<>();

        Pipeline<String> test = null; // new AsyncPipeline(work::add, 1);

        Processor<String, List<String>> processor =
                test.to(Captures.toList())
                .create();

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