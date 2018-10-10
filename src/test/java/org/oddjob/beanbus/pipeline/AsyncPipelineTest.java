package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AsyncPipelineTest {

    @Test
    public void whenStartJustConnectedToEndThenWorks() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Pipeline<String> test = AsyncPipeline2.start(executor);

        Processor<String, List<String>> start =
                test.to(Captures.toList(),
                        AsyncPipeline2.withOptions().async())
                        .create();

        start.accept("Apple");

        List<String> results = start.complete();

        assertThat(results, is(Arrays.asList("Apple")));

        executor.shutdown();
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

        Join<String, String> join = test.join();
        join.join(begin);

        Processor<String, List<String>> processor =
                join.to(Captures.toList())
                        .create();

        processor.accept("Apple");

        List<String> results = processor.complete();

        assertThat(results, is(Arrays.asList("Apple").stream().collect(Collectors.toList())));
    }

    @Test
    public void completeBlocks() {

        List<Runnable> work = new ArrayList<>();

        Pipeline<String> test = AsyncPipeline2.start(work::add);

        Processor<String, List<String>> start =
                test.to(Captures.toList(), AsyncPipeline2.withOptions().async())
                        .create();

        start.accept("Apple");

        AtomicBoolean isBlocked = new AtomicBoolean(true);

        new Thread(() -> {
            isBlocked.set(false);
            work.get(0).run();
        }).start();

        List<String> results = start.complete();
        assertThat(isBlocked.get(), is(false));

        assertThat(results, is(Arrays.asList("Apple")));
    }

    @Test
    public void workBlocks() {

        List<Runnable> work = new ArrayList<>();

        AsyncPipeline2<String> test = AsyncPipeline2.start(work::add);

        Processor<String, List<String>> start =
                test.to(Captures.toList(), AsyncPipeline2.withOptions().async().maxWork(1))
                        .create();

        Processor<String, List<String>> processor =
                test.to(Captures.toList())
                        .create();

        processor.accept("Apple");

        new Thread(() -> {
            while(!test.isBlocked()) {
                Thread.yield();
            }
            work.get(0).run();
        }).start();

        processor.accept("Pear");

        assertThat(work.size(), is(2));

        work.get(1).run();

        List<String> results = processor.complete();

        assertThat(results, is(Arrays.asList("Apple", "Pear")));
    }
}