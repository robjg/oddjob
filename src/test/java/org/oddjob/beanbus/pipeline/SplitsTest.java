package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SplitsTest {

    @Test
    public void testSimpleSplitSync() {

        testSimpleSplit(SyncPipeline.start(), SyncPipeline.withOptions());

    }

    @Test
    public void testSimpleSplitAsync() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        testSimpleSplit(AsyncPipeline2.start(executor), AsyncPipeline2.withOptions().async());

        executor.shutdown();
    }

    private void testSimpleSplit(Pipeline<Integer> pipeline, Pipeline.Options options ) {

        Link<Integer, Integer> split =
                pipeline.to(Splits.byIndex(i -> Collections.singleton(i % 2)),
                        options);

        Join<Integer, Integer> join = pipeline.join();

        join.join(split.to(Mapper.identity()));
        join.join(split.to(Mapper.identity()));

        Processor<Integer, Set<Integer>> processor =
                join.to(Captures.toSet(), options)
                        .create();

        processor.accept(1);
        processor.accept(2);
        processor.accept(3);

        Set<Integer> r = processor.complete();

        assertThat(r, is(Stream.of(1,2,3).collect(Collectors.toSet())));
    }

    @Test
    public void testSplitByNameSync() {

        testSplitByName(SyncPipeline.start(), SyncPipeline.withOptions());

    }

    @Test
    public void testSplitByNameAsync() {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        testSplitByName(AsyncPipeline2.start(executor), AsyncPipeline2.withOptions().async());

        executor.shutdown();
    }

    private void testSplitByName(Pipeline<String> pipeline, Pipeline.Options options ) {

        Link<String, String> split =
                pipeline.to(Splits.byName(data -> Collections.singleton(data)),
                        options);

        Join<String, String> join = pipeline.join();

        join.join(split.to(Mapper.with(s -> s + " banana"), options.named("yellow")));
        join.join(split.to(Mapper.with(s -> s + " apple"), options.named("green")));

        Processor<String, Set<String>> processor = join.to(Captures.toSet()).create();

        processor.accept("blue");
        processor.accept("green");
        processor.accept("yellow");

        Set<String> results = processor.complete();

        assertThat(results, is(
                Stream.of("yellow banana", "green apple").collect(Collectors.toSet())));
    }

}
